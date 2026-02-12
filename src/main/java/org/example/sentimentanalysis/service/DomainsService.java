package org.example.sentimentanalysis.service;

import org.example.sentimentanalysis.dto.responseDto.InferencePanelDto;
import org.example.sentimentanalysis.model.Domains;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 领域信息表 服务类
 * </p>
 *
 * @author kiang
 * @since 2026-02-11
 */
public interface DomainsService extends IService<Domains> {
   InferencePanelDto ListInferencePanelDto();

}
