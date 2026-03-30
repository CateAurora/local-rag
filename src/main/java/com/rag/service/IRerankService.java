package com.rag.service;

import com.rag.pojo.RerankResult;

import java.util.List;

/**
 * 重排序服务接口
 * <p>
 * 定义文档重排序的核心功能
 */
public interface IRerankService {

    /**
     * 重排序方法
     *
     * @param query    查询文本
     * @param passages 候选文本列表
     * @return 按相关性排序后的候选文本列表（带分数）
     */
    List<RerankResult> rerank(String query, List<String> passages);
}
