package org.example.sentimentanalysis.service;

import org.example.sentimentanalysis.dto.requestDto.TrainParaAddRec;
import org.example.sentimentanalysis.dto.requestDto.TrainParaQueryRec;
import org.example.sentimentanalysis.dto.responseDto.TrainParaListSend;
import org.example.sentimentanalysis.model.TrainPara;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 训练参数配置模板表 服务类
 * </p>
 *
 * @author kiang
 * @since 2026-02-17
 */
public interface TrainParaService extends IService<TrainPara> {

    TrainParaListSend listTrainPara(TrainParaQueryRec trainParaQueryRec);

    void addTrainPara(TrainParaAddRec trainPara);

    void deleteByIds(Long[] ids);
}
