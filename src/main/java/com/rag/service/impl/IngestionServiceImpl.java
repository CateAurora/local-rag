package com.rag.service.impl;

import com.rag.service.IDocumentParserService;
import com.rag.service.IIngestionService;
import com.rag.service.ITextSplitterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档摄入服务实现
 * <p>
 * 负责将文档解析、分块、向量化并存储到向量数据库
 */
@Service
public class IngestionServiceImpl implements IIngestionService {

    private static final Logger logger = LoggerFactory.getLogger(IngestionServiceImpl.class);

    private final IDocumentParserService documentParserService;
    private final ITextSplitterService textSplitterService;
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;

    /**
     * 构造函数，注入所需的服务和模型
     *
     * @param documentParserService 文档解析服务
     * @param textSplitterService   文本分块服务
     * @param embeddingModel        嵌入模型
     * @param vectorStore           向量存储
     */
    public IngestionServiceImpl(IDocumentParserService documentParserService, 
                          ITextSplitterService textSplitterService, 
                          EmbeddingModel embeddingModel, 
                          VectorStore vectorStore) {
        this.documentParserService = documentParserService;
        this.textSplitterService = textSplitterService;
        this.embeddingModel = embeddingModel;
        this.vectorStore = vectorStore;
    }

    /**
     * 摄入文档到向量数据库
     * <p>
     * 步骤：
     * 1. 解析文件 → 文本
     * 2. 分块 → 多个 chunk
     * 3. 对每个 chunk 调用 vectorStore.add(List<Document>) 存入向量库
     *
     * @param file 要摄入的文件
     * @return 成功存储的块数
     * @throws Exception 当处理过程中发生错误时抛出
     */
    public int ingestDocument(File file) throws Exception {
        logger.info("开始摄入文档: {}", file.getName());

        // 1. 解析文件 → 文本
        String content = documentParserService.parse(file);

        // 2. 分块 → 多个 chunk
        List<String> chunks = textSplitterService.split(content);

        // 3. 对每个 chunk 创建 Document 并存储到向量库
        List<Document> documents = new ArrayList<>();
        LocalDateTime uploadTime = LocalDateTime.now();

        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            
            // 创建元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("filename", file.getName());
            metadata.put("chunkIndex", i);
            metadata.put("uploadTime", uploadTime);
            metadata.put("chunkSize", chunk.length());

            // 创建 Document
            Document document = new Document(chunk, metadata);
            documents.add(document);

            // 记录进度
            logger.info("正在处理块 {} / {}: 大小 = {}", i + 1, chunks.size(), chunk.length());
        }

        // 批量存储到向量库
        vectorStore.add(documents);
        logger.info("文档摄入完成，共存储 {} 个块", documents.size());

        return documents.size();
    }

}
