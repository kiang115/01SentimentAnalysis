package org.example.sentimentanalysis.service.impl;

import org.example.sentimentanalysis.model.InferenceTasks;
import org.example.sentimentanalysis.mapper.InferenceTasksMapper;
import org.example.sentimentanalysis.service.InferenceTasksService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 推理任务详情表 服务实现类
 * </p>
 *
 * @author kiang
 * @since 2026-02-10
 */
@Service
public class InferenceTasksServiceImpl extends ServiceImpl<InferenceTasksMapper, InferenceTasks> implements InferenceTasksService {

}
