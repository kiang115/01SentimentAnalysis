package org.example.sentimentanalysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
import org.example.sentimentanalysis.service.TrainDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @Override
    public TrainDataListSend listTrainDataList(TrainDataQueryRec queryRec) {
        int pageNum = queryRec.getPageNum() != null ? queryRec.getPageNum() : 1;
        int pageSize = queryRec.getPageSize() != null ? queryRec.getPageSize() : 8;

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
        Long domainId = queryRec.getDomainId();
        if (domainId != null) {
            wrapper.eq(TrainData::getDomainId, domainId);
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
        List<Domains> domains = domainsMapper.selectList(null);

        Map<Long, String> domainIdToName = domains.stream()
                .collect(Collectors.toMap(Domains::getDomainId, Domains::getDomainName, (a, b) -> a));

        List<TrainDataListSend.TrainDataInfo> infoList = trainDataListAssembler.toTrainDataInfoList(trainDatalist, domainIdToName);

        // Page 来自 PageHelper，带正确 total；用同一 PageInfo 仅替换 list 为 DTO 列表，避免逐字段拷贝
        @SuppressWarnings("unchecked")
        PageInfo<TrainDataListSend.TrainDataInfo> pageInfo = (PageInfo<TrainDataListSend.TrainDataInfo>) (PageInfo<?>) new PageInfo<>(trainDatalist);
        pageInfo.setList(infoList);

        List<TrainDataListSend.domainInfo> domainInfoList = trainDataListAssembler.toDomainInfoList(domains);

        return TrainDataListSend.builder()
                .pageInfo(pageInfo)
                .domains(domainInfoList)
                .build();
    }

    @Override
    public void addBySingleData(TrainDataAddRec addDataRec) {
        TrainData trainData = new TrainData();
        trainData.setContent(addDataRec.getContent()).setLabel(addDataRec.getLabel()).setDomainId(addDataRec.getDomainId()).setSource(DataSourceEnum.UPLOAD.getCode());
        this.save(trainData);
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
