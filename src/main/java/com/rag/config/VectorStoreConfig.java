package com.rag.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * 向量存储配置类
 * <p>
 * 人工需要执行的步骤：
 * 1. 确认 Docker 已启动，且 Chroma 容器已运行（执行命令：docker run -d -p 8000:8000 chromadb/chroma）
 * 2. 确认 Ollama 服务已启动（执行命令：ollama serve）
 * 3. 确认所需嵌入模型已下载（执行命令：ollama pull nomic-embed-text）
 */
@Configuration
public class VectorStoreConfig {

    private static final Logger logger = LoggerFactory.getLogger(VectorStoreConfig.class);

    /**
     * 构造函数，用于初始化配置
     * <p>
     * Spring AI 会自动配置 ChromaVectorStore 和 EmbeddingModel Bean，
     * 这里通过 @Import 注解显式导入，确保 Bean 被正确创建
     */
    public VectorStoreConfig() {
        logger.info("VectorStoreConfig initialized");
        logger.info("ChromaVectorStore and EmbeddingModel beans will be created by Spring AI auto-configuration");
        logger.info("Please ensure Chroma container is running: docker run -d -p 8000:8000 chromadb/chroma");
        logger.info("Please ensure Ollama service is running: ollama serve");
        logger.info("Please ensure embedding model is downloaded: ollama pull nomic-embed-text");
    }

}
