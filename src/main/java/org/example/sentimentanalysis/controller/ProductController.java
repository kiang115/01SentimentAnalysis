package org.example.sentimentanalysis.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.example.sentimentanalysis.dto.requestDto.ProductAddRec;
import org.example.sentimentanalysis.dto.requestDto.ProductCommentQueryRec;
import org.example.sentimentanalysis.dto.requestDto.ProductEditRec;
import org.example.sentimentanalysis.dto.responseDto.CommentListSend;
import org.example.sentimentanalysis.dto.responseDto.ProductDetailSend;
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
    @GetMapping("/ProductDetail/{productId}")
    public Response<ProductDetailSend> getProductDetail(@PathVariable Long productId) {
        return Response.data(productsService.getProductDetail(productId));
    }

    @Operation(summary = "新增商品")
    @PostMapping("/addProduct")
    public Response<Void> addProduct(@RequestBody @Valid ProductAddRec productAddRec) {
        productsService.addProduct(productAddRec);
        return Response.success();
    }

    @Operation(summary = "编辑商品信息")
    @PostMapping("/editProduct")
    public Response<Void> editProduct(@RequestBody @Valid ProductEditRec productEditRec) {
        productsService.editProduct(productEditRec);
        return Response.success();
    }

    @Operation(summary = "删除商品")
    @PostMapping("/Productdelete/{productId}")
    public Response<Void> deleteProduct(@PathVariable Long productId) {
        productsService.deleteProduct(productId);
        return Response.success();
    }
}
