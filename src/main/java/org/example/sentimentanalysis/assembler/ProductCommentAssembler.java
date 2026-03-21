package org.example.sentimentanalysis.assembler;

import org.example.sentimentanalysis.dto.responseDto.CommentListSend;
import org.example.sentimentanalysis.enums.CommentFinalSentimentEnum;
import org.example.sentimentanalysis.enums.CommentStatusEnum;
import org.example.sentimentanalysis.model.Comments;
import org.example.sentimentanalysis.model.InferenceRecords;
import org.example.sentimentanalysis.model.Users;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class ProductCommentAssembler {

    public List<CommentListSend.CommentInfo> toCommentInfoList(
            List<Comments> commentsList,
            Map<Long, Users> userMap,
            Map<Long, InferenceRecords> latestRecordMap) {

        if (commentsList == null) {
            return Collections.emptyList();
        }

        return commentsList.stream()
                .map(comment -> {
                    Users user = userMap == null ? null : userMap.get(comment.getCustomerId());
                    InferenceRecords record = latestRecordMap == null ? null : latestRecordMap.get(comment.getCommentId());
                    CommentStatusEnum statusEnum = comment.getStatus() == null ? null : CommentStatusEnum.getByCode(comment.getStatus());

                    return CommentListSend.CommentInfo.builder()
                            .commentId(comment.getCommentId())
                            .userId(comment.getCustomerId())
                            .userName(user == null ? null : user.getUserName())
                            .userAvatarUrl(user == null ? null : user.getAvatarUrl())
                            .content(comment.getContent())
                            .publishTime(comment.getPublishTime())
                            .statusId(comment.getStatus())
                            .statusName(statusEnum == null ? null : statusEnum.getMessage())
                            .finalSentimentCode(comment.getFinalSentiment())
                            .finalSentimentName(CommentFinalSentimentEnum.getNameByCode(comment.getFinalSentiment()))
                            .positiveProb(record == null ? null : record.getPositiveProb())
                            .negativeProb(record == null ? null : record.getNegativeProb())
                            .confidence(record == null ? null : record.getConfidence())
                            .build();
                })
                .toList();
    }
}

