package org.example.sentimentanalysis.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
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
import org.example.sentimentanalysis.dto.responseDto.ProductTagAnalyzeSend;
import org.example.sentimentanalysis.dto.responseDto.ProductTagStatsListSend;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.mapper.ProductsMapper;
import org.example.sentimentanalysis.model.*;
import org.example.sentimentanalysis.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
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
    private MerchantAuthService merchantAuthService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private InferenceRecordsService inferenceRecordsService;
    @Autowired
    private ProductCommentAssembler productCommentAssembler;
    @Autowired
    private DomainsService domainsService;
    @Autowired
    private TagService tagService;
    @Autowired
    private InspectService inspectService;
    @Autowired
    private ReputationHistoryService reputationHistoryService;

    @Value("${llm.deepseek.api-url:https://api.deepseek.com/chat/completions}")
    private String deepSeekApiUrl;
    @Value("${llm.deepseek.api-key:}")
    private String deepSeekApiKey;
    @Value("${llm.deepseek.model:deepseek-chat}")
    private String deepSeekModel;

    private final HttpClient httpClient = HttpClient.newHttpClient();

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
        Merchants merchant = merchantAuthService.getCurrentMerchantOrThrow();

        Products product = new Products()
                .setMerchantId(merchant.getMerchantsId())
                .setName(productAddRec.getProductName())
                .setDetails(productAddRec.getProductDetail())
                .setImageUrl(productAddRec.getImageUrl())
                .setPrice(productAddRec.getPrice())
                .setDomainId(merchant.getDomainId());

        this.save(product);
    }

    @Override
    public void editProduct(ProductEditRec productEditRec) {
        Products product = merchantAuthService.getOwnedProductOrThrow(productEditRec.getProductId());

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
        Products product = merchantAuthService.getOwnedProductOrThrow(productId);

        boolean success = this.removeById(product.getProductsId());
        if (!success) {
            throw new CustomBusinessException("删除商品失败, productId=" + productId);
        }
    }

    @Override
    @Transactional
    public ProductTagAnalyzeSend analyzeProductTags(Long productId) {
        Products product = merchantAuthService.getTagAnalyzableProductOrThrow(productId);
        if (product.getDomainId() == null) {
            throw new CustomBusinessException("商品领域不存在 productId=" + productId);
        }
        Domains domain = domainsService.getById(product.getDomainId());
        if (domain == null || domain.getDomainName() == null || domain.getDomainName().isBlank()) {
            throw new CustomBusinessException("领域不存在 domainId=" + product.getDomainId());
        }
        String productName = product.getName() == null ? "" : product.getName().trim();
        String domainName = domain.getDomainName().trim();

        // 1. 仅筛选当前商品未做标签推理的评论
        List<Comments> pendingComments = commentsService.list(new LambdaQueryWrapper<Comments>()
                .eq(Comments::getProductId, productId)
                .eq(Comments::getIsInspected, Boolean.FALSE)
                .orderByDesc(Comments::getPublishTime));
        if (pendingComments.isEmpty()) {
            return buildAnalyzeResult(productId, 0, 0, 0);
        }

        // 2. 查询该商品已有标签列表，供AI优先复用
        List<Inspect> productInspectList = inspectService.list(new LambdaQueryWrapper<Inspect>()
                .eq(Inspect::getProductId, productId));

        Set<Long> existingTagIds = productInspectList.stream()
                .map(Inspect::getTagId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<Long, Tag> existingTagMapById = existingTagIds.isEmpty()
                ? Collections.emptyMap()
                : tagService.listByIds(existingTagIds).stream().collect(Collectors.toMap(Tag::getTagId, t -> t, (a, b) -> a));

        List<String> existingTags = productInspectList.stream()
                .map(Inspect::getTagId)
                .map(existingTagMapById::get)
                .filter(Objects::nonNull)
                .map(Tag::getTagName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        // 3. 直接调用DeepSeek获取结构化标签分析结果
        String llmContent = callDeepSeekForTags(productId, productName, domainName, existingTags, pendingComments);
        JSONObject inferData = parseTagResultJson(llmContent);
        JSONArray resultArray = inferData.getJSONArray("result");
        if (resultArray == null) {
            throw new CustomBusinessException("标签分析失败: 缺少result字段");
        }

        // 4. 聚合模型返回标签结果，避免同一标签重复写入
        Map<String, int[]> mergedCountByTagName = new LinkedHashMap<>();
        for (int i = 0; i < resultArray.size(); i++) {
            JSONObject tagResult = resultArray.getJSONObject(i);
            if (tagResult == null || tagResult.getString("tagName") == null || tagResult.getString("tagName").trim().isEmpty()) {
                throw new CustomBusinessException("标签分析失败: 返回tagName为空");
            }
            Integer positiveCount = tagResult.getInteger("positiveCount");
            Integer negativeCount = tagResult.getInteger("negativeCount");
            Integer totalCount = tagResult.getInteger("totalCount");
            int positive = positiveCount == null ? 0 : positiveCount;
            int negative = negativeCount == null ? 0 : negativeCount;
            int total = totalCount == null ? positive + negative : totalCount;
            if (positive < 0 || negative < 0 || total < 0) {
                throw new CustomBusinessException("标签分析失败: 返回计数存在负数");
            }
            if (positive + negative != total) {
                throw new CustomBusinessException("标签分析失败: totalCount与正负计数不一致");
            }
            String tagName = tagResult.getString("tagName").trim();
            int[] merged = mergedCountByTagName.computeIfAbsent(tagName, key -> new int[]{0, 0, 0});
            merged[0] += positive;
            merged[1] += negative;
            merged[2] += total;
        }
        if (mergedCountByTagName.isEmpty()) {
            markCommentsAsInspected(pendingComments);
            return buildAnalyzeResult(productId, pendingComments.size(), 0, 0);
        }

        // 5. 先按标签名查全局tag，不存在则新增
        List<String> resultTagNames = new ArrayList<>(mergedCountByTagName.keySet());
        Map<String, Tag> existingGlobalTagMap = tagService.list(new LambdaQueryWrapper<Tag>()
                        .in(Tag::getTagName, resultTagNames))
                .stream()
                .collect(Collectors.toMap(Tag::getTagName, t -> t, (a, b) -> a));

        List<Tag> newTags = resultTagNames.stream()
                .filter(tagName -> !existingGlobalTagMap.containsKey(tagName))
                .map(tagName -> new Tag().setTagName(tagName))
                .toList();
        if (!newTags.isEmpty()) {
            tagService.saveBatch(newTags);
        }

        // 6. 回查所有结果标签ID，并更新当前商品的inspect统计
        Map<String, Tag> allResultTagMap = tagService.list(new LambdaQueryWrapper<Tag>()
                        .in(Tag::getTagName, resultTagNames))
                .stream()
                .collect(Collectors.toMap(Tag::getTagName, t -> t, (a, b) -> a));
        Set<Long> resultTagIds = allResultTagMap.values().stream().map(Tag::getTagId).collect(Collectors.toSet());
        Map<Long, Inspect> inspectMapByTagId = resultTagIds.isEmpty()
                ? Collections.emptyMap()
                : inspectService.list(new LambdaQueryWrapper<Inspect>()
                        .eq(Inspect::getProductId, productId)
                        .in(Inspect::getTagId, resultTagIds)).stream()
                .collect(Collectors.toMap(Inspect::getTagId, i -> i, (a, b) -> a));

        List<Inspect> insertInspectList = new ArrayList<>();
        List<Inspect> updateInspectList = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : mergedCountByTagName.entrySet()) {
            String tagName = entry.getKey();
            int[] count = entry.getValue();
            Tag targetTag = allResultTagMap.get(tagName);
            if (targetTag == null || targetTag.getTagId() == null) {
                throw new CustomBusinessException("标签分析失败: 标签ID不存在 tagName=" + tagName);
            }
            Inspect inspect = inspectMapByTagId.get(targetTag.getTagId());
            if (inspect == null) {
                insertInspectList.add(new Inspect()
                        .setProductId(productId)
                        .setTagId(targetTag.getTagId())
                        .setPositiveCount(count[0])
                        .setNegativeCount(count[1])
                        .setTotalCount(count[2]));
            } else {
                inspect.setPositiveCount((inspect.getPositiveCount() == null ? 0 : inspect.getPositiveCount()) + count[0]);
                inspect.setNegativeCount((inspect.getNegativeCount() == null ? 0 : inspect.getNegativeCount()) + count[1]);
                inspect.setTotalCount((inspect.getTotalCount() == null ? 0 : inspect.getTotalCount()) + count[2]);
                updateInspectList.add(inspect);
            }
        }
        if (!insertInspectList.isEmpty()) {
            inspectService.saveBatch(insertInspectList);
        }
        if (!updateInspectList.isEmpty()) {
            inspectService.updateBatchById(updateInspectList);
        }

        // 7. 将本次参与分析评论标记为已标签推理
        markCommentsAsInspected(pendingComments);

        return buildAnalyzeResult(productId, pendingComments.size(), mergedCountByTagName.size(), newTags.size());
    }

    private ProductTagAnalyzeSend buildAnalyzeResult(Long productId, int processedCommentCount, int touchedTagCount, int newTagCount) {
        return ProductTagAnalyzeSend.builder()
                .productId(productId)
                .processedCommentCount(processedCommentCount)
                .touchedTagCount(touchedTagCount)
                .newTagCount(newTagCount)
                .build();
    }

    private void markCommentsAsInspected(List<Comments> pendingComments) {
        if (pendingComments == null || pendingComments.isEmpty()) {
            return;
        }

        List<Comments> commentsToUpdate = pendingComments.stream()
                .map(comment -> new Comments().setCommentId(comment.getCommentId()).setIsInspected(Boolean.TRUE))
                .toList();
        boolean updated = commentsService.updateBatchById(commentsToUpdate);
        if (!updated) {
            throw new CustomBusinessException("标签分析完成后更新评论状态失败");
        }
    }

    @Override
    public List<ProductTagStatsListSend.ProductTagStats> listProductTagStats(Long productId) {
        if (productId == null || productId <= 0) {
            throw new CustomBusinessException("商品ID不合法");
        }
        Products product = this.getById(productId);
        if (product == null) {
            throw new CustomBusinessException("商品不存在 productId=" + productId);
        }

        List<Inspect> inspectList = inspectService.list(new LambdaQueryWrapper<Inspect>()
                .eq(Inspect::getProductId, productId)
                .orderByDesc(Inspect::getUpdateTime));
        if (inspectList.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> tagIds = inspectList.stream().map(Inspect::getTagId).collect(Collectors.toSet());
        Map<Long, Tag> tagMapById = tagService.listByIds(tagIds).stream()
                .collect(Collectors.toMap(Tag::getTagId, t -> t, (a, b) -> a));

        return inspectList.stream().map(inspect -> {
            Tag tag = tagMapById.get(inspect.getTagId());
            if (tag == null) {
                throw new CustomBusinessException("标签数据不存在 tagId=" + inspect.getTagId());
            }
            return ProductTagStatsListSend.ProductTagStats.builder()
                    .tagId(tag.getTagId())
                    .tagName(tag.getTagName())
                    .positiveCount(inspect.getPositiveCount())
                    .negativeCount(inspect.getNegativeCount())
                    .totalCount(inspect.getTotalCount())
                    .updateTime(inspect.getUpdateTime())
                    .build();
        }).toList();
    }

    /**
     * 调用 DeepSeek 标签分析，返回模型 message.content。
     */
    private String callDeepSeekForTags(Long productId,
                                       String productName,
                                       String domainName,
                                       List<String> existingTags,
                                       List<Comments> pendingComments) {
        if (deepSeekApiKey == null || deepSeekApiKey.isBlank()) {
            throw new CustomBusinessException("DeepSeek API Key 未配置");
        }

        JSONObject payload = new JSONObject();
        payload.put("model", deepSeekModel);

        JSONArray messages = new JSONArray();
        JSONObject systemMsg = new JSONObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", buildSystemPrompt());
        messages.add(systemMsg);

        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", buildUserPrompt(productId, productName, domainName, existingTags, pendingComments));
        messages.add(userMsg);

        payload.put("messages", messages);
        payload.put("temperature", 0);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(deepSeekApiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + deepSeekApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload.toJSONString()))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new CustomBusinessException("DeepSeek调用失败, status=" + response.statusCode());
            }
            JSONObject respJson = JSON.parseObject(response.body());
            JSONArray choices = respJson.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                throw new CustomBusinessException("DeepSeek调用失败: choices为空");
            }
            JSONObject message = choices.getJSONObject(0).getJSONObject("message");
            if (message == null || message.getString("content") == null || message.getString("content").isBlank()) {
                throw new CustomBusinessException("DeepSeek调用失败: content为空");
            }
            return message.getString("content");
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new CustomBusinessException("DeepSeek调用异常: " + e.getMessage());
        }
    }

    /**
     * 解析模型返回的 JSON 内容，支持 ```json 包裹。
     */
    private JSONObject parseTagResultJson(String content) {
        String cleaned = content == null ? "" : content.trim();
        if (cleaned.startsWith("```")) {
            int firstNewline = cleaned.indexOf('\n');
            int lastFence = cleaned.lastIndexOf("```");
            if (firstNewline > -1 && lastFence > firstNewline) {
                cleaned = cleaned.substring(firstNewline + 1, lastFence).trim();
            }
            if (cleaned.startsWith("json")) {
                cleaned = cleaned.substring(4).trim();
            }
        }
        try {
            return JSON.parseObject(cleaned);
        } catch (Exception e) {
            throw new CustomBusinessException("标签分析失败: 返回不是合法JSON");
        }
    }

    /**
     * 系统提示词：强约束输出格式与归并策略。
     */
    private String buildSystemPrompt() {
        return """
                你是评论标签情感分析器。你必须严格输出 JSON，不得输出任何解释文字。
                规则：
                1. 输入里会给 productName、domainName、existingTags 和 comments。
                2. 你必须结合商品名和所属领域理解评论里提到的商品属性，再进行标签归并。
                3. 只有确实无法归并时，才允许创建新标签。新标签为中文，2-4字之间，以2字为最佳。
                4. 统计每个标签的 positiveCount、negativeCount、totalCount，且 totalCount=positiveCount+negativeCount。
                5. 情感只有两类：正向=positive，负向=negative。
                6. 输出必须是一个JSON对象，格式如下：
                {
                  "result": [
                    {
                      "tagName": "配送速度",
                      "positiveCount": 0,
                      "negativeCount": 2,
                      "totalCount": 2
                    }
                  ]
                }
                """;
    }

    /**
     * 用户提示词：传入产品、已有标签、待分析评论。
     */
    private String buildUserPrompt(Long productId,
                                   String productName,
                                   String domainName,
                                   List<String> existingTags,
                                   List<Comments> pendingComments) {
        JSONArray comments = new JSONArray();
        for (Comments item : pendingComments) {
            JSONObject comment = new JSONObject();
            comment.put("commentId", item.getCommentId());
            comment.put("content", item.getContent());
            comments.add(comment);
        }

        JSONObject input = new JSONObject();
        input.put("productId", productId);
        input.put("productName", productName);
        input.put("domainName", domainName);
        input.put("existingTags", existingTags);
        input.put("comments", comments);

        return "请基于以下输入完成标签情感聚合分析并仅返回JSON：\n" + input.toJSONString();
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

    @Override
    public void processCommentCorrection(Comments comment, Integer oldFinalSentiment, Integer newFinalSentiment) {
        if (comment == null) {
            throw new CustomBusinessException("评论不能为空");
        }
        if (comment.getProductId() == null || comment.getProductId() <= 0) {
            throw new CustomBusinessException("评论关联商品ID不合法");
        }
        if (comment.getMerchantId() == null || comment.getMerchantId() <= 0) {
            throw new CustomBusinessException("评论关联商铺ID不合法");
        }
        if (oldFinalSentiment == null || newFinalSentiment == null) {
            throw new CustomBusinessException("评论情感结果不能为空");
        }
        if (oldFinalSentiment.equals(newFinalSentiment)) {
            throw new CustomBusinessException("评论情感结果未发生变化");
        }

        Products product = this.getById(comment.getProductId());
        if (product == null) {
            throw new CustomBusinessException("商品不存在 productId=" + comment.getProductId());
        }
        Merchants merchant = merchantsService.getById(comment.getMerchantId());
        if (merchant == null) {
            throw new CustomBusinessException("商铺不存在 merchantId=" + comment.getMerchantId());
        }

        int positiveDelta = calculatePositiveDelta(oldFinalSentiment, newFinalSentiment);
        BigDecimal productPositiveRate = calculateCorrectedPositiveRate(
                product.getPositiveRate(),
                product.getInferredCount(),
                positiveDelta,
                "商品",
                product.getProductsId());
        BigDecimal merchantPositiveRate = calculateCorrectedPositiveRate(
                merchant.getPositiveRate(),
                merchant.getInferredCount(),
                positiveDelta,
                "商铺",
                merchant.getMerchantsId());

        boolean updateProductSuccess = this.updateById(new Products()
                .setProductsId(product.getProductsId())
                .setPositiveRate(productPositiveRate));
        if (!updateProductSuccess) {
            throw new CustomBusinessException("更新商品好评率失败 productId=" + product.getProductsId());
        }

        boolean updateMerchantSuccess = merchantsService.updateById(new Merchants()
                .setMerchantsId(merchant.getMerchantsId())
                .setPositiveRate(merchantPositiveRate));
        if (!updateMerchantSuccess) {
            throw new CustomBusinessException("更新商铺好评率失败 merchantId=" + merchant.getMerchantsId());
        }
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
        //5. 写入历史口碑数据表
        reputationHistoryService.recordProductReputation(productsToUpdate);

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
        reputationHistoryService.recordMerchantReputation(merchantsToUpdate);
    }


    private int calculatePositiveDelta(Integer oldFinalSentiment, Integer newFinalSentiment) {
        if (Objects.equals(oldFinalSentiment, 0) && Objects.equals(newFinalSentiment, 1)) {
            return 1;
        }
        if (Objects.equals(oldFinalSentiment, 1) && Objects.equals(newFinalSentiment, 0)) {
            return -1;
        }
        throw new CustomBusinessException("评论情感翻转结果不合法");
    }

    private BigDecimal calculateCorrectedPositiveRate(BigDecimal oldPositiveRate,
                                                      Long inferredCount,
                                                      int positiveDelta,
                                                      String targetType,
                                                      Long targetId) {
        if (inferredCount == null || inferredCount <= 0) {
            throw new CustomBusinessException(targetType + "已推理评论数不合法 id=" + targetId);
        }

        BigDecimal count = BigDecimal.valueOf(inferredCount);
        BigDecimal currentPositiveRate = oldPositiveRate == null ? BigDecimal.ZERO : oldPositiveRate;
        BigDecimal currentPositiveCount = currentPositiveRate.multiply(count);
        return currentPositiveCount
                .add(BigDecimal.valueOf(positiveDelta))
                .divide(count, 4, RoundingMode.HALF_UP);
    }
}
