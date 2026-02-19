package org.example.sentimentanalysis.service;

import org.example.sentimentanalysis.dto.requestDto.TrainResultRec;
import org.example.sentimentanalysis.model.Models;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 模型表 服务类
 * </p>
 *
 * @author kiang
 * @since 2026-02-11
 */
public interface ModelsService extends IService<Models> {
    Long addModelByTrainResult(TrainResultRec trainRec);
}
