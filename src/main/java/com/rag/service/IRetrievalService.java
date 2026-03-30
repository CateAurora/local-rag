package com.rag.service;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * 检索服务接口
 * <p>
 * 定义文档检索的核心功能
 */
public interface IRetrievalService {

    /**
     * 检索相关文档
     *
     * @param query 查询文本
     * @param topK  返回的文档数量（如果为 0，则使用默认值）
     * @return 按相关性排序后的文档列表
     */
    List<Document> retrieve(String query, int topK);

}
