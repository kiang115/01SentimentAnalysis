package org.example.sentimentanalysis.utils;

import com.aliyun.sdk.service.oss2.OSSClient;
import com.aliyun.sdk.service.oss2.models.PutObjectRequest;
import com.aliyun.sdk.service.oss2.models.PutObjectResult;
import com.aliyun.sdk.service.oss2.transport.BinaryData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class OssUploadUtil {

    // 1. 使用 @Value 注入配置 (注意加上 ${} 占位符)
    @Value("${oss.bucketName}")
    private String bucketName;

    @Value("${oss.bucketDomain}")
    private String domain;

    // 2. 使用 @Autowired 注入单例 OSSClient
    @Autowired
    private OSSClient ossClient;

    /**
     * 上传图片并返回访问地址
     */
    public String uploadImage(MultipartFile file) {
        // 逻辑对齐：日期文件夹 + UUID文件名
        String folderName = "images/" + DateFormatUtils.format(new Date(), "yyyyMMdd");
        String fileName = UUID.randomUUID().toString().replace("-", "");

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            // 业务异常抛出规范 [3]
            throw new RuntimeException("无效的文件名");
        }
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filePath = folderName + "/" + fileName + fileExtension;

        try {
            // 3. 采用 SDK V2 标准的 body(BinaryData) 方式上传 [4]
            PutObjectRequest putObjectRequest = PutObjectRequest.newBuilder()
                    .bucket(bucketName)
                    .key(filePath)
                    // 使用 BinaryData.fromStream 包装文件流
                    .body(BinaryData.fromStream(file.getInputStream()))
                    .build();

            // 执行上传并获取结果
            PutObjectResult result = ossClient.putObject(putObjectRequest);

            // 日志记录请求ID，便于排查问题 [4]
            log.info("OSS上传成功, RequestId: {}, ETag: {}", result.requestId(), result.eTag());

            // 4. 返回拼接后的完整访问路径
            String baseUrl = domain;
            return baseUrl + filePath;

        } catch (Exception e) {
            log.error("上传错误: {}", e.getMessage());
            // 遵循开发规范：严禁内部捕获异常并返回脏数据，直接抛出 [3]
            throw new RuntimeException("图片上传至云存储失败");
        }
    }
}

