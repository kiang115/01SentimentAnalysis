package org.example.sentimentanalysis.assembler;

import org.example.sentimentanalysis.dto.requestDto.InferPanelRec;
import org.example.sentimentanalysis.dto.responseDto.InferDataSend;
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
    public InferDataSend.InferenceDomainData toInferenceDomainData(
            InferPanelRec.InferenceDomainPara para,
            Models model,
            Domains domain,
            List<Comments> commentsList) {

        if (domain == null || model == null) {
            return null;
        }

        // 转换评论列表
        List<InferDataSend.InferenceDomainComment> commentDtos = commentsList.stream()
                .map(this::toCommentDto)
                .toList();

        return InferDataSend.InferenceDomainData.builder()
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
    private InferDataSend.InferenceDomainComment toCommentDto(Comments comment) {
        return InferDataSend.InferenceDomainComment.builder()
                .commentId(comment.getCommentId())
                .content(comment.getContent())
                .build();
    }
}

