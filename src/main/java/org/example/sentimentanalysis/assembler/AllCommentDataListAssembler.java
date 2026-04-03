package org.example.sentimentanalysis.assembler;

import org.example.sentimentanalysis.dto.responseDto.AllCommentDataListSend;
import org.example.sentimentanalysis.enums.CommentFinalSentimentEnum;
import org.example.sentimentanalysis.enums.CommentStatusEnum;
import org.example.sentimentanalysis.model.Comments;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.model.InferenceRecords;
import org.example.sentimentanalysis.model.Merchants;
import org.example.sentimentanalysis.model.Products;
import org.example.sentimentanalysis.model.Users;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class AllCommentDataListAssembler {

    public List<AllCommentDataListSend.AllCommentDataInfo> toAllCommentDataInfoList(
            List<Comments> commentsList,
            Map<Long, Domains> domainMap,
            Map<Long, Products> productMap,
            Map<Long, Merchants> merchantMap,
            Map<Long, Users> userMap,
            Map<Long, InferenceRecords> latestRecordMap) {

        if (commentsList == null) {
            return Collections.emptyList();
        }

        return commentsList.stream()
                .map(comment -> {
                    Domains domain = domainMap == null ? null : domainMap.get(comment.getDomainId());
                    Products product = productMap == null ? null : productMap.get(comment.getProductId());
                    Merchants merchant = merchantMap == null ? null : merchantMap.get(comment.getMerchantId());
                    Users user = userMap == null ? null : userMap.get(comment.getCustomerId());
                    InferenceRecords record = latestRecordMap == null ? null : latestRecordMap.get(comment.getCommentId());
                    CommentStatusEnum statusEnum = comment.getStatus() == null ? null : CommentStatusEnum.getByCode(comment.getStatus());

                    return AllCommentDataListSend.AllCommentDataInfo.builder()
                            .commentId(comment.getCommentId())
                            .content(comment.getContent())
                            .inferResultCode(record == null ? null : record.getModelSentiment())
                            .inferResultName(record == null ? null : CommentFinalSentimentEnum.getNameByCode(record.getModelSentiment()))
                            .positiveProb(record == null ? null : record.getPositiveProb())
                            .negativeProb(record == null ? null : record.getNegativeProb())
                            .domainId(comment.getDomainId())
                            .domainName(domain == null ? null : domain.getDomainName())
                            .productId(comment.getProductId())
                            .productName(product == null ? null : product.getName())
                            .productImageUrl(product == null ? null : product.getImageUrl())
                            .merchantId(comment.getMerchantId())
                            .merchantName(merchant == null ? null : merchant.getName())
                            .merchantAvatarUrl(merchant == null ? null : merchant.getAvatarUrl())
                            .customerName(user == null ? null : user.getUserName())
                            .customerAvatarUrl(user == null ? null : user.getAvatarUrl())
                            .statusId(comment.getStatus())
                            .statusName(statusEnum == null ? null : statusEnum.getMessage())
                            .publishTime(comment.getPublishTime())
                            .finalSentimentCode(comment.getFinalSentiment())
                            .finalSentimentName(CommentFinalSentimentEnum.getNameByCode(comment.getFinalSentiment()))
                            .isInspected(comment.getIsInspected())
                            .build();
                })
                .toList();
    }
}
