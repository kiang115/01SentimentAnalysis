package org.example.sentimentanalysis.service;

import org.example.sentimentanalysis.dto.responseDto.InferenceTasksDto;
import org.example.sentimentanalysis.model.InferenceTasks;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 推理任务详情表 服务类
 * </p>
 *
 * @author kiang
 * @since 2026-02-11
 */
public interface InferenceTasksService extends IService<InferenceTasks> {

    List<InferenceTasksDto> listAllTasks();
}
