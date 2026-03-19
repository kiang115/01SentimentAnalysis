package org.example.sentimentanalysis.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.example.sentimentanalysis.response.Response;
import org.example.sentimentanalysis.utils.OssUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileController {

    @Autowired
    private OssUploadUtil ossUploadUtil;

    @Operation(summary = "上传文件到阿里云OSS")
    @PostMapping("/file/upload")
    public Response<String> upload(@RequestParam MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomBusinessException("上传文件不能为空");
        }
        String url = ossUploadUtil.uploadImage(file);
        return Response.data(url);
    }
}
