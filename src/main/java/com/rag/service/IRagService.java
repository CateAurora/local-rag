package com.rag.service;

import com.rag.pojo.RagResult;
import com.rag.pojo.SourceInfo;

/**
 * RAG 服务接口
 * <p>
 * 定义 RAG 问答的核心功能
 */
public interface IRagService {

    /**
     * 回答问题
     *
     * @param question 问题
     * @return RAG 结果，包含答案和来源
     */
    RagResult ask(String question);
}
