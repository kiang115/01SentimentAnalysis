package org.example.sentimentanalysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.example.sentimentanalysis.dto.requestDto.TrainDataAddRec;
import org.example.sentimentanalysis.dto.requestDto.TrainDataQueryRec;
import org.example.sentimentanalysis.dto.responseDto.TrainDataListSend;
import org.example.sentimentanalysis.assembler.TrainDataListAssembler;
import org.example.sentimentanalysis.enums.DataSourceEnum;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.mapper.DomainsMapper;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.model.TrainData;
import org.example.sentimentanalysis.mapper.TrainDataMapper;
import org.example.sentimentanalysis.response.Response;
import org.example.sentimentanalysis.service.DomainsService;
import org.example.sentimentanalysis.service.TrainDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 训练数据表 服务实现类
 * </p>
 *
 * @author kiang
 * @since 2026-02-17
 */
@Service
public class TrainDataServiceImpl extends ServiceImpl<TrainDataMapper, TrainData> implements TrainDataService {

    @Autowired
    private DomainsMapper domainsMapper;
    @Autowired
    private TrainDataListAssembler trainDataListAssembler;
    @Autowired
    @Lazy
    private DomainsService domainsService;

    @Override
    public TrainDataListSend listTrainDataList(TrainDataQueryRec queryRec) {
        int pageNum = queryRec.getPageNum() != null ? queryRec.getPageNum() : 1;
        int pageSize = queryRec.getPageSize() != null ? queryRec.getPageSize() : 8;

        if (queryRec.getDomainId() != null) {
            domainsService.checkIdExist(queryRec.getDomainId());
        }

        LambdaQueryWrapper<TrainData> wrapper = new LambdaQueryWrapper<>();
        String content = queryRec.getContent();
        if (content != null && !content.isBlank()) {
            wrapper.like(TrainData::getContent, content);
        }
        Integer label = queryRec.getLabel();
        if (label != null && (label == 0 || label == 1)) {
            wrapper.eq(TrainData::getLabel, label);
        }
        String source = queryRec.getSource();
        if (source != null && DataSourceEnum.isCodeExist(source)) {
            wrapper.eq(TrainData::getSource, source);
        }

        String orderName = queryRec.getOrderName();
        String order = queryRec.getOrder();
        if ("time".equalsIgnoreCase(orderName) && ("desc".equalsIgnoreCase(order) || "asc".equalsIgnoreCase(order))) {
            wrapper.orderBy(true, "asc".equalsIgnoreCase(order), TrainData::getCreatedAt);
        } else if ("count".equalsIgnoreCase(orderName) && ("desc".equalsIgnoreCase(order) || "asc".equalsIgnoreCase(order))) {
            wrapper.orderBy(true, "asc".equalsIgnoreCase(order), TrainData::getTrainCount);
        } else {
            wrapper.orderByAsc(TrainData::getId);
        }

        PageHelper.startPage(pageNum, pageSize);

        List<TrainData> trainDatalist = this.list(wrapper);

        Map<Long, String> domainIdToName = domainsService.getDomainIdToName();

        List<TrainDataListSend.TrainDataInfo> infoList = trainDataListAssembler.toTrainDataInfoList(trainDatalist, domainIdToName);

        // Page 来自 PageHelper，带正确 total；用同一 PageInfo 仅替换 list 为 DTO 列表，避免逐字段拷贝
        @SuppressWarnings("unchecked")
        PageInfo<TrainDataListSend.TrainDataInfo> pageInfo = (PageInfo<TrainDataListSend.TrainDataInfo>) (PageInfo<?>) new PageInfo<>(trainDatalist);
        pageInfo.setList(infoList);

        return TrainDataListSend.builder()
                .pageInfo(pageInfo)
                .domains(domainsService.listAllDomainsInfo())
                .build();
    }

    @Override
    public void addBySingleData(TrainDataAddRec addDataRec) {
        TrainData trainData = new TrainData();
        trainData.setContent(addDataRec.getContent()).setLabel(addDataRec.getLabel()).setDomainId(addDataRec.getDomainId()).setSource(DataSourceEnum.UPLOAD.getCode());
        this.save(trainData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importCsv(MultipartFile file, Long domainId) throws IOException {
        // 1. 使用新的 Builder 模式替换已弃用的 with... 方法
        CSVFormat csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT)
                .setHeader()              // 自动处理表头
                .setSkipHeaderRecord(true) // 跳过表头行
                .setIgnoreHeaderCase(true) // 忽略表头大小写
                .setTrim(true)             // 自动去空格
                .get();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = csvFormat.parse(reader)) {

            List<TrainData> dataList = new ArrayList<>();

            for (CSVRecord record : csvParser) {
                // 使用 Optional 或简单的判空简化逻辑
                String content = record.isMapped("content") ? record.get("content") : null;
                String labelStr = record.isMapped("label") ? record.get("label") : null;

                // --- 校验逻辑 ---
                if (content == null || content.isBlank() || labelStr == null) {
                    continue;
                }

                try {
                    int label = Integer.parseInt(labelStr);
                    if (label != 0 && label != 1) continue;

                    // --- 构建对象 (建议使用构造函数或 Builder) ---
                    TrainData data = new TrainData();
                    data.setDomainId(domainId);
                    data.setContent(content);
                    data.setLabel(label);
                    data.setSource("upload");
                    data.setTrainCount(0);

                    dataList.add(data);
                } catch (NumberFormatException e) {
                    // label 不是数字，忽略此行
                }
            }

            // 2. 利用 MyBatis Plus 自身的分批插入功能
            // 第二个参数 1000 表示每 1000 条执行一次 SQL 插入，无需手动写 if(size >= 1000)
            if (!dataList.isEmpty()) {
                this.saveBatch(dataList, 1000);
            }
        }
    }

    @Override
    public void deleteByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new CustomBusinessException("操作失败：请选择要删除的数据");
        }

        // 1. 去重，防止前端传了重复的ID导致数量对不上
        List<Long> distinctIds = Arrays.stream(ids).distinct().collect(Collectors.toList());

        // 2. 查询数据库里实际存在的数量
        long count = this.count(new LambdaQueryWrapper<TrainData>()
                .in(TrainData::getId, distinctIds));

        // 3. 对比数量
        if (count != distinctIds.size()) {
            throw new CustomBusinessException("操作失败：请求的部分数据不存在或已被删除");
        }
        this.removeBatchByIds(distinctIds);
    }


    @Override
    public void updateTrainCount(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<Long> distinctIds = ids.stream().distinct().toList();
        // 一次查询，并校验“任意一个找不到抛异常”
        List<TrainData> existingList = listByIds(distinctIds);
        Set<Long> existingIds = existingList.stream().map(TrainData::getId).collect(Collectors.toSet());
        List<Long> missingIds = distinctIds.stream().filter(id -> !existingIds.contains(id)).toList();
        if (!missingIds.isEmpty()) {
            throw new CustomBusinessException("训练数据不存在或已删除，id=" + missingIds);
        }
        // 原子自增，一条 SQL
        LambdaUpdateWrapper<TrainData> wrapper = new LambdaUpdateWrapper<TrainData>()
                .setSql("train_count = train_count + 1")
                .in(TrainData::getId, distinctIds);
        update(wrapper);
    }
}
