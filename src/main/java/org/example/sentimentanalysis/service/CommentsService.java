package org.example.sentimentanalysis.service;

import org.example.sentimentanalysis.dto.requestDto.InferenceResultDto;
import org.example.sentimentanalysis.dto.responseDto.InferenceDataDto;
import org.example.sentimentanalysis.model.Comments;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 商家情感分析-评论主表 服务类
 * </p>
 *
 * @author kiang
 * @since 2026-02-11
 */
public interface CommentsService extends IService<Comments> {

    void updateCommentStatus(InferenceDataDto inferenceDataDto);

    void updateCommentStatus(InferenceResultDto inferenceResultDto, int code);
}
