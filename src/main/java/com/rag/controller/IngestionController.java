package com.rag.controller;

import com.rag.service.IIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * 文档摄入控制器
 * <p>
 * 提供 REST 接口，用于上传和处理文档
 */
@RestController
@RequestMapping("/documents")
public class IngestionController {

    private static final Logger logger = LoggerFactory.getLogger(IngestionController.class);
    private final IIngestionService ingestionService;

    /**
     * 构造函数，注入 IngestionService
     *
     * @param ingestionService 文档摄入服务
     */
    public IngestionController(IIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    /**
     * 上传文档接口
     * <p>
     * 接收 MultipartFile，调用 IngestionService 处理，返回成功块数
     * <p>
     * 人工测试方法：
     * 1. 使用 Postman：设置请求方法为 POST，URL 为 http://localhost:8080/documents/upload
     *    - 在 Body 选项卡中选择 form-data
     *    - 添加 key 为 "file"，类型为 "File"，选择要上传的文件
     * 2. 使用 curl：
     *    curl -X POST -F "file=@/path/to/file.pdf" http://localhost:8080/documents/upload
     *
     * @param file 要上传的文件
     * @return 包含成功块数的响应
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file) {
        logger.info("接收到文件上传请求: {}", file.getOriginalFilename());

        try {
            // 将 MultipartFile 转换为临时文件
            File tempFile = File.createTempFile("upload-", "-" + file.getOriginalFilename());
            file.transferTo(tempFile);

            // 调用 IngestionService 处理文件
            int chunkCount = ingestionService.ingestDocument(tempFile);

            // 删除临时文件
            tempFile.delete();

            // 返回成功响应
            logger.info("文件上传处理完成，成功存储 {} 个块", chunkCount);
            return ResponseEntity.ok("文件上传成功，成功存储 " + chunkCount + " 个块");
        } catch (IOException e) {
            logger.error("文件上传失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            logger.error("文档处理失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("文档处理失败: " + e.getMessage());
        }
    }

}
