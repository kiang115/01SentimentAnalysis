package org.example.sentimentanalysis.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.example.sentimentanalysis.dto.requestDto.ProductAddRec;
import org.example.sentimentanalysis.dto.requestDto.ProductEditRec;
import org.example.sentimentanalysis.dto.responseDto.ProductTagAnalyzeSend;
import org.example.sentimentanalysis.dto.responseDto.ProductDetailSend;
import org.example.sentimentanalysis.dto.responseDto.ProductTagStatsListSend;
import org.example.sentimentanalysis.response.Response;
import org.example.sentimentanalysis.service.ProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {

    @Autowired
    private ProductsService productsService;

    @Operation(summary = "查询商品详情")
    @SaCheckRole(value = {"consumer", "merchant", "admin"}, mode = SaMode.OR)
    @GetMapping("/ProductDetail/{productId}")
    public Response<ProductDetailSend> getProductDetail(@PathVariable Long productId) {
        return Response.data(productsService.getProductDetail(productId));
    }

    @Operation(summary = "新增商品")
    @SaCheckRole(value = {"merchant", "admin"}, mode = SaMode.OR)
    @PostMapping("/addProduct")
    public Response<Void> addProduct(@RequestBody @Valid ProductAddRec productAddRec) {
        productsService.addProduct(productAddRec);
        return Response.success();
    }

    @Operation(summary = "编辑商品信息")
    @SaCheckRole(value = {"merchant", "admin"}, mode = SaMode.OR)
    @PostMapping("/editProduct")
    public Response<Void> editProduct(@RequestBody @Valid ProductEditRec productEditRec) {
        productsService.editProduct(productEditRec);
        return Response.success();
    }

    @Operation(summary = "删除商品")
    @SaCheckRole(value = {"merchant", "admin"}, mode = SaMode.OR)
    @PostMapping("/Productdelete/{productId}")
    public Response<Void> deleteProduct(@PathVariable Long productId) {
        productsService.deleteProduct(productId);
        return Response.success();
    }

    @Operation(summary = "按商品触发标签情感分析")
    @SaCheckRole(value = {"merchant", "admin"}, mode = SaMode.OR)
    @PostMapping("/ProductAnalyzeTags/{productId}")
    public Response<ProductTagAnalyzeSend> analyzeProductTags(@PathVariable Long productId) {
        return Response.data(productsService.analyzeProductTags(productId));
    }

    @Operation(summary = "查询商品标签统计")
    @SaCheckRole(value = {"consumer", "merchant", "admin"}, mode = SaMode.OR)
    @PostMapping("/ProductTagStats/{productId}")
    public Response<ProductTagStatsListSend> listProductTagStats(@PathVariable Long productId) {
        ProductTagStatsListSend productTagStatsListSend = new ProductTagStatsListSend();
        productTagStatsListSend.setProductTagStatsList(productsService.listProductTagStats(productId));
        return Response.data(productTagStatsListSend);
    }
}
