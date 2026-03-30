package com.rag.config;

import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.ChromaVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 向量存储配置测试类
 * <p>
 * 用于验证 ChromaVectorStore 和 EmbeddingModel 能正常注入
 */
@SpringBootTest
public class VectorStoreConfigTest {

    /**
     * 注入 ChromaVectorStore Bean
     */
    @Autowired
    private ChromaVectorStore chromaVectorStore;

    /**
     * 注入 EmbeddingModel Bean
     */
    @Autowired
    private EmbeddingModel embeddingModel;

    /**
     * 测试 ChromaVectorStore 是否正常注入
     * <p>
     * 验证 chromaVectorStore 不为 null
     */
    @Test
    public void testChromaVectorStoreInjection() {
        assertThat(chromaVectorStore).isNotNull();
    }

    /**
     * 测试 EmbeddingModel 是否正常注入
     * <p>
     * 验证 embeddingModel 不为 null
     */
    @Test
    public void testEmbeddingModelInjection() {
        assertThat(embeddingModel).isNotNull();
    }

}
