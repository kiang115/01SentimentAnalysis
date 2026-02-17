package org.example.sentimentanalysis.service.impl;

import org.example.sentimentanalysis.model.TrainData;
import org.example.sentimentanalysis.mapper.TrainDataMapper;
import org.example.sentimentanalysis.service.TrainDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
