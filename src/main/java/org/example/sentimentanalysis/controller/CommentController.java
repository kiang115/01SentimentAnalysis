package org.example.sentimentanalysis.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.example.sentimentanalysis.dto.requestDto.CommentAddRec;
import org.example.sentimentanalysis.dto.requestDto.ProductCommentQueryRec;
import org.example.sentimentanalysis.dto.responseDto.CommentListSend;
import org.example.sentimentanalysis.response.Response;
import org.example.sentimentanalysis.service.CommentsService;
import org.example.sentimentanalysis.service.ProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

//评论发布+评论展示接口
@RestController
public class CommentController {

    @Autowired
    private ProductsService productsService;
    @Autowired
    private CommentsService commentsService;

    @Operation(summary = "查询商品评论列表")
    @PostMapping("/ProductComments")
    public Response<CommentListSend> listProductComments(@RequestBody @Valid  ProductCommentQueryRec queryRec) {
        return Response.data(productsService.listProductComments(queryRec));
    }

    @Operation(summary = "发布评论")
    @SaCheckRole(value = {"consumer", "admin"}, mode = SaMode.OR)
    @PostMapping("/addComment")
    public Response<Void> addComment(@RequestBody @Valid CommentAddRec rec) {
        commentsService.addComment(rec);
        return Response.success();
    }
}
