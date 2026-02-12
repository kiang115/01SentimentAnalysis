package org.example.sentimentanalysis.assembler;

import org.example.sentimentanalysis.dto.requestDto.InferenceParaDto;
import org.example.sentimentanalysis.dto.responseDto.InferenceDataDto;
import org.example.sentimentanalysis.model.Comments;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.model.Models;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InferenceDataAssembler {

    /**
     * 将实体对象和参数转换为前端需要的 InferenceDomainData DTO
     */
    public InferenceDataDto.InferenceDomainData toInferenceDomainData(
            InferenceParaDto.InferenceDomainPara para,
            Models model,
            Domains domain,
            List<Comments> commentsList) {

        if (domain == null || model == null) {
            return null;
        }

        // 转换评论列表
        List<InferenceDataDto.InferenceDomainComment> commentDtos = commentsList.stream()
                .map(this::toCommentDto)
                .toList();

        return InferenceDataDto.InferenceDomainData.builder()
                .domainId(domain.getDomainId())
                .domainUrl(domain.getDomainUrl())
                .modelId(model.getModelId())
                .modelVersion(model.getModelVersion())
                .inferenceCommentNums(para.getInferenceReviewNums()) // 使用请求中的参数值
                .inferenceDomainCommentList(commentDtos)
                .build();
    }

    /**
     * 内部私有方法：转换单条评论
     */
    private InferenceDataDto.InferenceDomainComment toCommentDto(Comments comment) {
        return InferenceDataDto.InferenceDomainComment.builder()
                .commentId(comment.getCommentId())
                .content(comment.getContent())
                .build();
    }
}

