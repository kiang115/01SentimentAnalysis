package org.example.sentimentanalysis.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.sentimentanalysis.assembler.AllCommentDataListAssembler;
import org.example.sentimentanalysis.dto.requestDto.AllCommentDataListQueryRec;
import org.example.sentimentanalysis.dto.requestDto.CommentAddRec;
import org.example.sentimentanalysis.dto.requestDto.InferResultRec;
import org.example.sentimentanalysis.dto.responseDto.AllCommentDataListSend;
import org.example.sentimentanalysis.dto.responseDto.InferDataSend;
import org.example.sentimentanalysis.dto.responseDto.InferPieChartSend;
import org.example.sentimentanalysis.enums.CommentFinalSentimentEnum;
import org.example.sentimentanalysis.enums.CommentStatusEnum;
import org.example.sentimentanalysis.enums.UserTypeEnum;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.model.*;
import org.example.sentimentanalysis.mapper.CommentsMapper;
import org.example.sentimentanalysis.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    private AllCommentDataListAssembler allCommentDataListAssembler;
    @Autowired
    @Lazy
    private ProductsService productsService;
    @Autowired
    @Lazy
    private MerchantsService merchantsService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private TrainDataService trainDataService;
    @Autowired
    @Lazy
    private DomainsService domainsService;
    @Autowired
    private InferenceRecordsService inferenceRecordsService;
    @Autowired
    @Lazy
    private ModelsService modelsService;


    @Override
    public AllCommentDataListSend listAllCommentDataList(AllCommentDataListQueryRec queryRec) {
        int pageNum = (queryRec.getPageNum() == null || queryRec.getPageNum() <= 0) ? 1 : queryRec.getPageNum();
        int pageSize = (queryRec.getPageSize() == null || queryRec.getPageSize() <= 0) ? 8 : queryRec.getPageSize();

        if (queryRec.getDomainId() != null) {
            domainsService.checkIdExist(queryRec.getDomainId());
        }

        LambdaQueryWrapper<Comments> wrapper = new LambdaQueryWrapper<>();
        if (queryRec.getContent() != null && !queryRec.getContent().isBlank()) {
            wrapper.like(Comments::getContent, queryRec.getContent().trim());
        }
        if (queryRec.getStatusId() != null && CommentStatusEnum.getByCode(queryRec.getStatusId()) != null) {
            wrapper.eq(Comments::getStatus, queryRec.getStatusId());
        }
        if (queryRec.getLabel() != null && (queryRec.getLabel() == 0 || queryRec.getLabel() == 1)) {
            wrapper.eq(Comments::getFinalSentiment, queryRec.getLabel());
        }
        if (queryRec.getDomainId() != null) {
            wrapper.eq(Comments::getDomainId, queryRec.getDomainId());
        }
        if ("asc".equalsIgnoreCase(queryRec.getTimeOrder())) {
            wrapper.orderByAsc(Comments::getPublishTime);
        } else {
            wrapper.orderByDesc(Comments::getPublishTime);
        }

        PageHelper.startPage(pageNum, pageSize);
        List<Comments> commentsList = this.list(wrapper);
        PageInfo<Comments> commentsPageInfo = new PageInfo<>(commentsList);

        Set<Long> domainIds = commentsList.stream()
                .map(Comments::getDomainId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Domains> domainMap = domainIds.isEmpty()
                ? Collections.emptyMap()
                : domainsService.listByIds(domainIds).stream()
                .collect(Collectors.toMap(Domains::getDomainId, d -> d, (a, b) -> a));

        Set<Long> productIds = commentsList.stream()
                .map(Comments::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Products> productMap = productIds.isEmpty()
                ? Collections.emptyMap()
                : productsService.listByIds(productIds).stream()
                .collect(Collectors.toMap(Products::getProductsId, p -> p, (a, b) -> a));

        Set<Long> merchantIds = commentsList.stream()
                .map(Comments::getMerchantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Merchants> merchantMap = merchantIds.isEmpty()
                ? Collections.emptyMap()
                : merchantsService.listByIds(merchantIds).stream()
                .collect(Collectors.toMap(Merchants::getMerchantsId, m -> m, (a, b) -> a));

        Set<Long> customerIds = commentsList.stream()
                .map(Comments::getCustomerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Users> userMap = customerIds.isEmpty()
                ? Collections.emptyMap()
                : usersService.listByIds(customerIds).stream()
                .collect(Collectors.toMap(Users::getUserId, u -> u, (a, b) -> a));

        List<Long> commentIds = commentsList.stream()
                .map(Comments::getCommentId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, InferenceRecords> latestRecordMap = new LinkedHashMap<>();
        if (!commentIds.isEmpty()) {
            List<InferenceRecords> records = inferenceRecordsService.list(
                    new LambdaQueryWrapper<InferenceRecords>()
                            .in(InferenceRecords::getCommentId, commentIds)
                            .orderByDesc(InferenceRecords::getInferenceTime)
                            .orderByDesc(InferenceRecords::getInferenceId)
            );
            for (InferenceRecords record : records) {
                latestRecordMap.putIfAbsent(record.getCommentId(), record);
            }
        }

        Set<Long> modelIds = latestRecordMap.values().stream()
                .map(InferenceRecords::getModelId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Models> modelMap = modelIds.isEmpty()
                ? Collections.emptyMap()
                : modelsService.listByIds(modelIds).stream()
                .collect(Collectors.toMap(Models::getModelId, m -> m, (a, b) -> a));

        List<AllCommentDataListSend.AllCommentDataInfo> infoList = allCommentDataListAssembler.toAllCommentDataInfoList(
                commentsList,
                domainMap,
                productMap,
                merchantMap,
                userMap,
                latestRecordMap,
                modelMap
        );

        @SuppressWarnings("unchecked")
        PageInfo<AllCommentDataListSend.AllCommentDataInfo> pageInfo =
                (PageInfo<AllCommentDataListSend.AllCommentDataInfo>) (PageInfo<?>) commentsPageInfo;
        pageInfo.setList(infoList);

        return AllCommentDataListSend.builder()
                .pageInfo(pageInfo)
                .domains(domainsService.listAllDomainsInfo())
                .commentStatusList(listCommentStatusInfo())
                .build();
    }

    @Override
    public List<AllCommentDataListSend.CommentStatusInfo> listCommentStatusInfo() {
        return Arrays.stream(CommentStatusEnum.values())
                .map(statusEnum -> AllCommentDataListSend.CommentStatusInfo.builder()
                        .statusCode(statusEnum.getCode())
                        .statusName(statusEnum.getMessage())
                        .build())
                .toList();
    }

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
        String content = rec.getContent() == null ? null : rec.getContent().trim();
        if (content == null || content.isEmpty()) {
            throw new CustomBusinessException("评论内容不能为空");
        }

        Long customerId = StpUtil.getLoginIdAsLong();
        if (customerId == null || customerId <= 0) {
            throw new CustomBusinessException("当前登录用户ID不合法");
        }

        Products product = productsService.getById(rec.getProductId());
        if (product == null) {
            throw new CustomBusinessException("商品不存在 productId=" + rec.getProductId());
        }
        Long merchantId = product.getMerchantId();
        if (merchantId == null || merchantId <= 0) {
            throw new CustomBusinessException("商品未绑定商铺 productId=" + rec.getProductId());
        }

        Comments comment = new Comments()
                .setCustomerId(customerId)
                .setContent(content)
                .setDomainId(rec.getDomainId())
                .setProductId(rec.getProductId())
                .setMerchantId(merchantId)
                .setStatus(CommentStatusEnum.PENDING.getCode());
//        将商品count和商铺count+1
        productsService.lambdaUpdate()
                .eq(Products::getProductsId, rec.getProductId())
                .setSql("comment_count = comment_count + 1") // 注意：这里写的是数据库字段名
                .update();
        merchantsService.lambdaUpdate()
                .eq(Merchants::getMerchantsId, merchantId)
                .setSql("comment_count = comment_count + 1") // 注意：这里写的是数据库字段名
                .update();
        boolean success = this.save(comment);
        if (!success) {
            throw new CustomBusinessException("发布评论失败");
        }
    }

    @Override
    @Transactional
    public void reviewComment(Long commentId) {
        if (commentId == null || commentId <= 0) {
            throw new CustomBusinessException("评论ID不合法");
        }

        Comments comment = this.getById(commentId);
        if (comment == null) {
            throw new CustomBusinessException("评论不存在 commentId=" + commentId);
        }

        if (!Objects.equals(comment.getStatus(), CommentStatusEnum.INFERRED.getCode())) {
            throw new CustomBusinessException("仅已推理评论允许进入审核 commentId=" + commentId);
        }

        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (currentUserId == null || currentUserId <= 0) {
            throw new CustomBusinessException("当前登录用户ID不合法");
        }

        Users currentUser = usersService.getById(currentUserId);
        if (currentUser == null) {
            throw new CustomBusinessException("当前登录用户不存在 userId=" + currentUserId);
        }

        boolean isAdmin = Objects.equals(currentUser.getUserType(), UserTypeEnum.ADMIN.getCode());
        if (!isAdmin) {
            if (!Objects.equals(currentUser.getUserType(), UserTypeEnum.MERCHANT.getCode())) {
                throw new CustomBusinessException("当前用户无权审核评论");
            }
            if (!Objects.equals(comment.getMerchantId(), currentUser.getMerchantId())) {
                throw new CustomBusinessException("无权审核非本人商铺评论 commentId=" + commentId);
            }
        }

        boolean success = this.lambdaUpdate()
                .eq(Comments::getCommentId, commentId)
                .eq(Comments::getStatus, CommentStatusEnum.INFERRED.getCode())
                .set(Comments::getStatus, CommentStatusEnum.REVIEWING.getCode())
                .update();
        if (!success) {
            throw new CustomBusinessException("评论进入审核失败 commentId=" + commentId);
        }
    }

    @Override
    @Transactional
    public void rejectComment(Long commentId) {
        Comments comment = getReviewingCommentOrThrow(commentId);
        boolean success = this.lambdaUpdate()
                .eq(Comments::getCommentId, comment.getCommentId())
                .eq(Comments::getStatus, CommentStatusEnum.REVIEWING.getCode())
                .set(Comments::getStatus, CommentStatusEnum.REJECTED.getCode())
                .update();
        if (!success) {
            throw new CustomBusinessException("评论拒绝失败 commentId=" + commentId);
        }
    }

    @Override
    @Transactional
    public void correctComment(Long commentId) {
        Comments comment = getReviewingCommentOrThrow(commentId);
        Integer oldFinalSentiment = comment.getFinalSentiment();
        Integer newFinalSentiment = reverseFinalSentiment(oldFinalSentiment);

        boolean success = this.lambdaUpdate()
                .eq(Comments::getCommentId, comment.getCommentId())
                .eq(Comments::getStatus, CommentStatusEnum.REVIEWING.getCode())
                .set(Comments::getStatus, CommentStatusEnum.CORRECTED.getCode())
                .set(Comments::getFinalSentiment, newFinalSentiment)
                .update();
        if (!success) {
            throw new CustomBusinessException("评论修正失败 commentId=" + commentId);
        }

        comment.setStatus(CommentStatusEnum.CORRECTED.getCode())
                .setFinalSentiment(newFinalSentiment);
        trainDataService.addCorrectedComment(comment);
        productsService.processCommentCorrection(comment, oldFinalSentiment, newFinalSentiment);

//        修改model表中被correct的评论数
        InferenceRecords record = inferenceRecordsService.lambdaQuery()
                .eq(InferenceRecords::getCommentId, commentId)
                .select(InferenceRecords::getModelId)
                .last("LIMIT 1")
                .one();
        if (record == null || record.getModelId() == null) {
            throw new CustomBusinessException("待修正评论没有推理相关记录或推理模型不存在！");
        }
        // 2. 调用 modelsService 进行原子自增
        modelsService.lambdaUpdate()
                .eq(Models::getModelId, record.getModelId())
                .setSql("corrected_num = corrected_num + 1")
                .update();
    }

    private Comments getReviewingCommentOrThrow(Long commentId) {
        if (commentId == null || commentId <= 0) {
            throw new CustomBusinessException("评论ID不合法");
        }

        Comments comment = this.getById(commentId);
        if (comment == null) {
            throw new CustomBusinessException("评论不存在 commentId=" + commentId);
        }
        if (!Objects.equals(comment.getStatus(), CommentStatusEnum.REVIEWING.getCode())) {
            throw new CustomBusinessException("仅审核中评论允许执行审核结果操作 commentId=" + commentId);
        }
        return comment;
    }

    private Integer reverseFinalSentiment(Integer finalSentiment) {
        CommentFinalSentimentEnum current = CommentFinalSentimentEnum.getByCode(finalSentiment);
        if (current == null) {
            throw new CustomBusinessException("评论最终情感不合法");
        }
        return Objects.equals(current, CommentFinalSentimentEnum.POSITIVE)
                ? CommentFinalSentimentEnum.NEGATIVE.getCode()
                : CommentFinalSentimentEnum.POSITIVE.getCode();
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
