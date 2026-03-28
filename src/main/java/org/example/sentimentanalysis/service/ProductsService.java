package org.example.sentimentanalysis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.sentimentanalysis.dto.requestDto.InferResultRec;
import org.example.sentimentanalysis.dto.requestDto.ProductAddRec;
import org.example.sentimentanalysis.dto.requestDto.ProductCommentQueryRec;
import org.example.sentimentanalysis.dto.requestDto.ProductEditRec;
import org.example.sentimentanalysis.dto.responseDto.CommentListSend;
import org.example.sentimentanalysis.dto.responseDto.ProductTagAnalyzeSend;
import org.example.sentimentanalysis.dto.responseDto.ProductDetailSend;
import org.example.sentimentanalysis.dto.responseDto.ProductTagStatsListSend;
import org.example.sentimentanalysis.model.Comments;
import org.example.sentimentanalysis.model.Products;

import java.util.List;

/**
 * <p>
 * 鍟嗗搧淇℃伅琛?鏈嶅姟绫? * </p>
 *
 * @author kiang
 * @since 2026-03-17
 */
public interface ProductsService extends IService<Products> {

    ProductDetailSend getProductDetail(Long productId);

    CommentListSend listProductComments(ProductCommentQueryRec queryRec);

    void addProduct(ProductAddRec productAddRec);

    void editProduct(ProductEditRec productEditRec);

    void deleteProduct(Long productId);

    void processInferenceResults(List<InferResultRec.CommentResultList> results);

    void processCommentCorrection(Comments comment, Integer oldFinalSentiment, Integer newFinalSentiment);

    ProductTagAnalyzeSend analyzeProductTags(Long productId);

    List<ProductTagStatsListSend.ProductTagStats> listProductTagStats(Long productId);
}
