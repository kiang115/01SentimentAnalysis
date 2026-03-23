package org.example.sentimentanalysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.sentimentanalysis.dto.requestDto.CommentAddRec;
import org.example.sentimentanalysis.dto.requestDto.InferResultRec;
import org.example.sentimentanalysis.dto.responseDto.InferDataSend;
import org.example.sentimentanalysis.dto.responseDto.InferPieChartSend;
import org.example.sentimentanalysis.enums.CommentStatusEnum;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.model.Comments;
import org.example.sentimentanalysis.model.Domains;
import org.example.sentimentanalysis.mapper.CommentsMapper;
import org.example.sentimentanalysis.model.Merchants;
import org.example.sentimentanalysis.model.Products;
import org.example.sentimentanalysis.service.CommentsService;
import org.example.sentimentanalysis.service.DomainsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.sentimentanalysis.service.MerchantsService;
import org.example.sentimentanalysis.service.ProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    @Autowired
    @Lazy
    private ProductsService productsService;
    @Autowired
    @Lazy
    private MerchantsService merchantsService;


    @Override
    public void addComment(CommentAddRec rec) {
        if (rec == null) {
            throw new CustomBusinessException("评论参数不能为空");
        }
        if (rec.getDomainId() == null || rec.getDomainId() <= 0) {
            throw new CustomBusinessException("领域ID不合法");
        }
        if (rec.getProductId() == null || rec.getProductId() <= 0) {
            throw new CustomBusinessException("商品ID不合法");
        }
        if (rec.getMerchantId() == null || rec.getMerchantId() <= 0) {
            throw new CustomBusinessException("商铺ID不合法");
        }
        String content = rec.getContent() == null ? null : rec.getContent().trim();
        if (content == null || content.isEmpty()) {
            throw new CustomBusinessException("评论内容不能为空");
        }

        Long customerId = rec.getCustomerId() == null ? 1L : rec.getCustomerId();
        if (customerId <= 0) {
            throw new CustomBusinessException("顾客ID不合法");
        }

        Comments comment = new Comments()
                .setCustomerId(customerId)
                .setContent(content)
                .setDomainId(rec.getDomainId())
                .setProductId(rec.getProductId())
                .setMerchantId(rec.getMerchantId())
                .setStatus(CommentStatusEnum.PENDING.getCode());
//        将商品count和商铺count+1
        productsService.lambdaUpdate()
                .eq(Products::getProductsId, rec.getProductId())
                .setSql("comment_count = comment_count + 1") // 注意：这里写的是数据库字段名
                .update();
        merchantsService.lambdaUpdate()
                .eq(Merchants::getMerchantsId, rec.getMerchantId())
                .setSql("comment_count = comment_count + 1") // 注意：这里写的是数据库字段名
                .update();
        boolean success = this.save(comment);
        if (!success) {
            throw new CustomBusinessException("发布评论失败");
        }
    }

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
