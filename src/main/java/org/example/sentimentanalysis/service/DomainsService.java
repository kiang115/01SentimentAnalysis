package org.example.sentimentanalysis.service;

import org.example.sentimentanalysis.dto.commonDto.DomainsInfo;
import org.example.sentimentanalysis.dto.responseDto.InferPanelSend;
import org.example.sentimentanalysis.dto.responseDto.TrainPanelSend;
import org.example.sentimentanalysis.model.Domains;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 领域信息表 服务类
 * </p>
 *
 * @author kiang
 * @since 2026-02-11
 */
public interface DomainsService extends IService<Domains> {
    InferPanelSend ListInferencePanelDto();
    TrainPanelSend listTrainPanel();
    List<DomainsInfo> listAllDomainsInfo();
    Map<Long, String> getDomainIdToName();
//    增加校验工具函数
    void checkIdExist(Long domainId);
    void checkIdsExist(Long[] domainIds);

    String getDomainUrlById(Long domainId);
}
