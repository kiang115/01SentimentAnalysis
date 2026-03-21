package org.example.sentimentanalysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.sentimentanalysis.assembler.ProductCommentAssembler;
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
}
