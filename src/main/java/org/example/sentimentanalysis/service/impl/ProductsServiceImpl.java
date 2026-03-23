package org.example.sentimentanalysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.sentimentanalysis.assembler.ProductCommentAssembler;
import org.example.sentimentanalysis.dto.requestDto.InferResultRec;
import org.example.sentimentanalysis.dto.requestDto.ProductAddRec;
import org.example.sentimentanalysis.dto.requestDto.ProductCommentQueryRec;
import org.example.sentimentanalysis.dto.requestDto.ProductEditRec;
import org.example.sentimentanalysis.dto.responseDto.CommentListSend;
import org.example.sentimentanalysis.dto.responseDto.ProductDetailSend;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.mapper.ProductsMapper;
import org.example.sentimentanalysis.model.Comments;
import org.example.sentimentanalysis.model.InferenceRecords;
import org.example.sentimentanalysis.model.Merchants;
import org.example.sentimentanalysis.model.Products;
import org.example.sentimentanalysis.model.Users;
import org.example.sentimentanalysis.service.CommentsService;
import org.example.sentimentanalysis.service.InferenceRecordsService;
import org.example.sentimentanalysis.service.MerchantsService;
import org.example.sentimentanalysis.service.ProductsService;
import org.example.sentimentanalysis.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 商品信息表 服务实现类
 * </p>
 *
 * @author kiang
 * @since 2026-03-17
 */
@Service
public class ProductsServiceImpl extends ServiceImpl<ProductsMapper, Products> implements ProductsService {

    @Autowired
    private MerchantsService merchantsService;
    @Autowired
    private CommentsService commentsService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private InferenceRecordsService inferenceRecordsService;
    @Autowired
    private ProductCommentAssembler productCommentAssembler;

    @Override
    public ProductDetailSend getProductDetail(Long productId) {
        if (productId == null || productId <= 0) {
            throw new CustomBusinessException("商品ID不合法");
        }

        Products product = this.getById(productId);
        if (product == null) {
            throw new CustomBusinessException("商品不存在 productId=" + productId);
        }

        ProductDetailSend.ProductDetailSendBuilder builder = ProductDetailSend.builder()
                .productId(product.getProductsId())
                .name(product.getName())
                .imageUrl(product.getImageUrl())
                .details(product.getDetails())
                .price(product.getPrice())
                .rating(product.getRating())
                .commentCount(product.getCommentCount())
                .positiveRate(product.getPositiveRate())
                .domainId(product.getDomainId())
                .merchantId(product.getMerchantId());

        if (product.getMerchantId() != null) {
            Merchants merchant = merchantsService.getById(product.getMerchantId());
            if (merchant != null) {
                builder.merchantName(merchant.getName());
            }
        }

        return builder.build();
    }

    @Override
    public CommentListSend listProductComments(ProductCommentQueryRec queryRec) {
        if (queryRec == null || queryRec.getProductId() == null) {
            throw new CustomBusinessException("商品ID不能为空");
        }

        Long productId = queryRec.getProductId();
        if (productId <= 0) {
            throw new CustomBusinessException("商品ID不合法");
        }

        Products product = this.getById(productId);
        if (product == null) {
            throw new CustomBusinessException("商品不存在 productId=" + productId);
        }

        int pageNum = (queryRec.getPageNum() == null || queryRec.getPageNum() <= 0) ? 1 : queryRec.getPageNum();
        int pageSize = (queryRec.getPageSize() == null || queryRec.getPageSize() <= 0) ? 10 : queryRec.getPageSize();

        LambdaQueryWrapper<Comments> commentWrapper = new LambdaQueryWrapper<Comments>()
                .eq(Comments::getProductId, productId)
                .orderByDesc(Comments::getPublishTime);

        PageHelper.startPage(pageNum, pageSize);
        List<Comments> commentsList = commentsService.list(commentWrapper);
        PageInfo<Comments> commentsPageInfo = new PageInfo<>(commentsList);

        Set<Long> userIds = commentsList.stream()
                .map(Comments::getCustomerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Users> userMap;
        if (userIds.isEmpty()) {
            userMap = Collections.emptyMap();
        } else {
            userMap = usersService.listByIds(userIds).stream()
                    .collect(Collectors.toMap(Users::getUserId, u -> u, (a, b) -> a));
        }

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

        List<CommentListSend.CommentInfo> commentInfoList = productCommentAssembler.toCommentInfoList(commentsList, userMap, latestRecordMap);

        @SuppressWarnings("unchecked")
        PageInfo<CommentListSend.CommentInfo> resultPageInfo = (PageInfo<CommentListSend.CommentInfo>) (PageInfo<?>) commentsPageInfo;
        resultPageInfo.setList(commentInfoList);

        return CommentListSend.builder()
                .pageInfo(resultPageInfo)
                .build();
    }

    @Override
    public void addProduct(ProductAddRec productAddRec) {
        if (productAddRec.getMerchantId() <= 0) {
            throw new CustomBusinessException("商铺ID不合法");
        }

        Merchants merchant = merchantsService.getById(productAddRec.getMerchantId());
        if (merchant == null) {
            throw new CustomBusinessException("商铺不存在 merchantId=" + productAddRec.getMerchantId());
        }

        Products product = new Products()
                .setMerchantId(productAddRec.getMerchantId())
                .setName(productAddRec.getProductName())
                .setDetails(productAddRec.getProductDetail())
                .setImageUrl(productAddRec.getImageUrl())
                .setPrice(productAddRec.getPrice())
                .setDomainId(merchant.getDomainId());

        this.save(product);
    }

    @Override
    public void editProduct(ProductEditRec productEditRec) {
        if (productEditRec.getProductId() <= 0) {
            throw new CustomBusinessException("商品ID不合法");
        }

        Products product = this.getById(productEditRec.getProductId());
        if (product == null) {
            throw new CustomBusinessException("商品不存在 productId=" + productEditRec.getProductId());
        }

        product.setName(productEditRec.getProductName())
                .setDetails(productEditRec.getProductDetail())
                .setImageUrl(productEditRec.getImageUrl())
                .setPrice(productEditRec.getPrice());

        boolean success = this.updateById(product);
        if (!success) {
            throw new CustomBusinessException("编辑商品失败, productId=" + productEditRec.getProductId());
        }
    }

    @Override
    public void deleteProduct(Long productId) {
        if (productId == null || productId <= 0) {
            throw new CustomBusinessException("商品ID不合法");
        }

        Products product = this.getById(productId);
        if (product == null) {
            throw new CustomBusinessException("商品不存在 productId=" + productId);
        }

        boolean success = this.removeById(productId);
        if (!success) {
            throw new CustomBusinessException("删除商品失败, productId=" + productId);
        }
    }

    @Override
    public void processInferenceResults(List<InferResultRec.CommentResultList> results) {
        if (results == null || results.isEmpty()) return;

        // 1. 获取所有评论ID并批量查询评论基础信息（获取关联的 productId 和 merchantId）
        List<Long> commentIds = results.stream()
                .map(InferResultRec.CommentResultList::getCommentId)
                .collect(Collectors.toList());

        // 使用 Service 层的 listByIds
        Map<Long, Comments> commentMap = commentsService.listByIds(commentIds).stream()
                .collect(Collectors.toMap(Comments::getCommentId, c -> c));

        // 2. 独立调用商品批量更新函数
        this.updateProductBatch(results, commentMap);

        // 3. 独立调用商家批量更新函数
        this.updateMerchantBatch(results, commentMap);
    }

    /**
     * 函数 A: 批量增量更新商品评分及好评率
     */
    private void updateProductBatch(List<InferResultRec.CommentResultList> results, Map<Long, Comments> commentMap) {
        // 1. 在内存中按 ProductId 分组聚合
        Map<Long, List<InferResultRec.CommentResultList>> productGroup = results.stream()
                .filter(res -> commentMap.containsKey(res.getCommentId()))
                .filter(res -> commentMap.get(res.getCommentId()).getProductId() != null)
                .filter(res -> res.getPositiveProb() != null)
                .collect(Collectors.groupingBy(res -> commentMap.get(res.getCommentId()).getProductId()));

        if (productGroup.isEmpty()) return;

        // 2. 批量查询所有涉及到的商品当前信息
        List<Products> productsToUpdate = this.listByIds(productGroup.keySet());

        // 3. 内存计算增量分值与好评数
        for (Products product : productsToUpdate) {
            List<InferResultRec.CommentResultList> batchRecords = productGroup.get(product.getProductsId());

            // A. 本批次增量统计
            // 评分增量总和 (positiveProb * 10)
            BigDecimal batchSumScore = batchRecords.stream()
                    .map(r -> r.getPositiveProb().multiply(new BigDecimal("10")))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 好评数增量 (modelSentiment == 1)
            long batchPositiveCount = batchRecords.stream()
                    .filter(r -> r.getModelSentiment() != null && r.getModelSentiment() == 1)
                    .count();

            int batchSize = batchRecords.size();

            // B. 获取旧数据并处理 NULL
            long oldCount = product.getInferredCount() == null ? 0L : product.getInferredCount();
            BigDecimal oldRating = product.getRating() == null ? BigDecimal.ZERO : product.getRating();
            BigDecimal oldPositiveRate = product.getPositiveRate() == null ? BigDecimal.ZERO : product.getPositiveRate();

            // C. 增量公式计算
            long newInferredCount = oldCount + batchSize;
            BigDecimal newInferredCountBD = new BigDecimal(newInferredCount);

            // 新评分: (oldRating * oldCount + batchSumScore) / newInferredCount
            BigDecimal newRating = oldRating.multiply(new BigDecimal(oldCount))
                    .add(batchSumScore)
                    .divide(newInferredCountBD, 2, RoundingMode.HALF_UP);

            // 新好评率: (oldPositiveRate * oldCount + batchPositiveCount) / newInferredCount
            // 数据库对应 DECIMAL(5, 4)，此处保留4位小数
            BigDecimal newPositiveRate = oldPositiveRate.multiply(new BigDecimal(oldCount))
                    .add(new BigDecimal(batchPositiveCount))
                    .divide(newInferredCountBD, 4, RoundingMode.HALF_UP);

            // D. 赋值回实体
            product.setRating(newRating);
            product.setPositiveRate(newPositiveRate);
            product.setInferredCount(newInferredCount);
            // 注意：不再修改 commentCount，保持其为总评论数
        }

        // 4. 批量写回数据库
        this.updateBatchById(productsToUpdate);
    }

    /**
     * 函数 B: 批量增量更新商家评分及好评率
     */
    private void updateMerchantBatch(List<InferResultRec.CommentResultList> results, Map<Long, Comments> commentMap) {
        // 1. 在内存中按 MerchantId 分组聚合
        Map<Long, List<InferResultRec.CommentResultList>> merchantGroup = results.stream()
                .filter(res -> commentMap.containsKey(res.getCommentId()))
                .filter(res -> commentMap.get(res.getCommentId()).getMerchantId() != null)
                .filter(res -> res.getPositiveProb() != null)
                .collect(Collectors.groupingBy(res -> commentMap.get(res.getCommentId()).getMerchantId()));

        if (merchantGroup.isEmpty()) return;

        // 2. 批量查询所有涉及到的商家信息
        List<Merchants> merchantsToUpdate = merchantsService.listByIds(merchantGroup.keySet());

        // 3. 内存计算
        for (Merchants merchant : merchantsToUpdate) {
            List<InferResultRec.CommentResultList> batchRecords = merchantGroup.get(merchant.getMerchantsId());

            // A. 本批次增量统计
            BigDecimal batchSumScore = batchRecords.stream()
                    .map(r -> r.getPositiveProb().multiply(new BigDecimal("10")))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long batchPositiveCount = batchRecords.stream()
                    .filter(r -> r.getModelSentiment() != null && r.getModelSentiment() == 1)
                    .count();

            int batchSize = batchRecords.size();

            // B. 获取旧数据并处理 NULL
            long oldCount = merchant.getInferredCount() == null ? 0L : merchant.getInferredCount();
            BigDecimal oldRating = merchant.getRating() == null ? BigDecimal.ZERO : merchant.getRating();
            BigDecimal oldPositiveRate = merchant.getPositiveRate() == null ? BigDecimal.ZERO : merchant.getPositiveRate();

            // C. 增量公式计算
            long newInferredCount = oldCount + batchSize;
            BigDecimal newInferredCountBD = new BigDecimal(newInferredCount);

            // 新评分
            BigDecimal newRating = oldRating.multiply(new BigDecimal(oldCount))
                    .add(batchSumScore)
                    .divide(newInferredCountBD, 2, RoundingMode.HALF_UP);

            // 新好评率
            BigDecimal newPositiveRate = oldPositiveRate.multiply(new BigDecimal(oldCount))
                    .add(new BigDecimal(batchPositiveCount))
                    .divide(newInferredCountBD, 4, RoundingMode.HALF_UP);

            // D. 赋值回实体
            merchant.setRating(newRating);
            merchant.setPositiveRate(newPositiveRate);
            merchant.setInferredCount(newInferredCount);
        }

        // 4. 批量写回数据库
        merchantsService.updateBatchById(merchantsToUpdate);
    }
}
