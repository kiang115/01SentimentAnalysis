package org.example.sentimentanalysis.service.impl;

import org.example.sentimentanalysis.dto.responseDto.TrainPanelDTO;
import org.example.sentimentanalysis.model.TrainTasks;
import org.example.sentimentanalysis.mapper.TrainTasksMapper;
import org.example.sentimentanalysis.service.TrainTasksService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 训练任务管理表 服务实现类
 * </p>
 *
 * @author kiang
 * @since 2026-02-17
 */
@Service
public class TrainTasksServiceImpl extends ServiceImpl<TrainTasksMapper, TrainTasks> implements TrainTasksService {

}
