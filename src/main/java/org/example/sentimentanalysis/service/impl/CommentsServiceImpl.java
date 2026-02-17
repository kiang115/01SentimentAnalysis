package org.example.sentimentanalysis.service.impl;

import org.example.sentimentanalysis.dto.requestDto.InferResultRec;
import org.example.sentimentanalysis.dto.responseDto.InferDataSend;
import org.example.sentimentanalysis.enums.CommentStatusEnum;
import org.example.sentimentanalysis.model.Comments;
import org.example.sentimentanalysis.mapper.CommentsMapper;
import org.example.sentimentanalysis.service.CommentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 商家情感分析-评论主表 服务实现类
 * </p>
 *
 * @author kiang
 * @since 2026-02-11
 */
@Service
public class CommentsServiceImpl extends ServiceImpl<CommentsMapper, Comments> implements CommentsService {

    @Override
    public void updateCommentStatus(InferDataSend inferDataSend) {
        List<InferDataSend.InferenceDomainData> domainDataList = inferDataSend.getInferenceDomainDataList();
        if (domainDataList == null || domainDataList.isEmpty()) {
            return;
        }
        List<Long> commentIds = domainDataList.stream()
                .filter(d -> d.getInferenceDomainCommentList() != null)
                .flatMap(d -> d.getInferenceDomainCommentList().stream())
                .map(InferDataSend.InferenceDomainComment::getCommentId)
                .collect(Collectors.toList());
        if (commentIds.isEmpty()) {
            return;
        }
        this.lambdaUpdate()
                .in(Comments::getCommentId, commentIds)
                .set(Comments::getStatus, CommentStatusEnum.INFERRING.getCode())
                .update();
    }

    @Override
    public void updateCommentStatus(InferResultRec inferResultRec, int code) {
        List<InferResultRec.CommentResultList> results = inferResultRec.getResults();
        if (results == null || results.isEmpty()) {
            return;
        }
        List<Long> commentIds = results.stream()
                .map(InferResultRec.CommentResultList::getCommentId)
                .collect(Collectors.toList());
        if (commentIds.isEmpty()) {
            return;
        }
        this.lambdaUpdate()
                .in(Comments::getCommentId, commentIds)
                .set(Comments::getStatus, code)
                .update();
    }
}
