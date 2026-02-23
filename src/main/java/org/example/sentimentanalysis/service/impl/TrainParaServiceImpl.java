package org.example.sentimentanalysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.sentimentanalysis.assembler.TrainDataListAssembler;
import org.example.sentimentanalysis.assembler.TrainParaAssembler;
import org.example.sentimentanalysis.dto.requestDto.TrainParaAddRec;
import org.example.sentimentanalysis.dto.requestDto.TrainParaQueryRec;
import org.example.sentimentanalysis.dto.responseDto.TrainParaListSend;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.mapper.DomainsMapper;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.model.TrainPara;
import org.example.sentimentanalysis.mapper.TrainParaMapper;
import org.example.sentimentanalysis.service.DomainsService;
import org.example.sentimentanalysis.service.TrainParaService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 训练参数配置模板表 服务实现类
 * </p>
 *
 * @author kiang
 * @since 2026-02-17
 */
@Service
public class TrainParaServiceImpl extends ServiceImpl<TrainParaMapper, TrainPara> implements TrainParaService {

    @Autowired
    private DomainsMapper domainsMapper;
    @Autowired
    private TrainDataListAssembler trainDataListAssembler;
    @Autowired
    @Lazy
    private DomainsService domainsService;
    @Autowired
    private TrainParaAssembler trainParaAssembler;

    @Override
    public TrainParaListSend listTrainPara(TrainParaQueryRec trainParaQueryRec) {
        int pageNum = trainParaQueryRec.getPageNum() != null ? trainParaQueryRec.getPageNum() : 1;
        int pageSize = trainParaQueryRec.getPageSize() != null ? trainParaQueryRec.getPageSize() : 8;

        LambdaQueryWrapper<TrainPara> wrapper = new LambdaQueryWrapper<>();
//        筛选相应的参数表
        Long domainId = trainParaQueryRec.getDomainId();
        Long paraId = trainParaQueryRec.getParaId();
        wrapper.eq(trainParaQueryRec.getDomainId() != null, TrainPara::getDomainId, domainId)
                .eq(trainParaQueryRec.getParaId() != null, TrainPara::getParaId, paraId);

        PageHelper.startPage(pageNum, pageSize);
        List<TrainPara> rawList = this.list(wrapper);

        Map<Long, String> domainIdToName = domainsService.getDomainIdToName();

        List<TrainParaListSend.TrainParaInfo> infoList = trainParaAssembler.toTrainParaList(rawList, domainIdToName);
        @SuppressWarnings("unchecked")
        PageInfo<TrainParaListSend.TrainParaInfo> pageInfo = (PageInfo<TrainParaListSend.TrainParaInfo>) (PageInfo<?>) new PageInfo<>(rawList);
        pageInfo.setList(infoList);
        return TrainParaListSend.builder()
                .pageInfo(pageInfo)
                .domainsInfo(domainsService.listAllDomainsInfo())
                .build();
    }

    @Override
    public void addTrainPara(TrainParaAddRec trainPara) {
        domainsService.checkIdExist(trainPara.getDomainId());

        TrainPara newTrainPara = new TrainPara();
        newTrainPara.setDomainId(trainPara.getDomainId()).setLoraR(trainPara.getLoraR()).setLoraAlpha(trainPara.getLoraAlpha())
                .setEpochs(trainPara.getEpochs()).setBatchSize(trainPara.getBatchSize()).setLearningRate(trainPara.getLearningRate())
                .setRandomSeed(trainPara.getRandomSeed()).setLoraModules(trainPara.getLoraModules())
                .setTrainSplitRatio(trainPara.getTrainSplitRatio());
        this.save(newTrainPara);
    }

    @Override
    public void deleteByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new CustomBusinessException("操作失败：请选择要删除的数据");
        }

        // 1. 去重，防止前端传了重复的ID导致数量对不上
        List<Long> distinctIds = Arrays.stream(ids).distinct().collect(Collectors.toList());

        // 2. 查询数据库里实际存在的数量
        long count = this.count(new LambdaQueryWrapper<TrainPara>()
                .in(TrainPara::getParaId, distinctIds));

        // 3. 对比数量
        if (count != distinctIds.size()) {
            throw new CustomBusinessException("操作失败：请求的部分数据不存在或已被删除");
        }
        this.removeBatchByIds(distinctIds);
    }
}
