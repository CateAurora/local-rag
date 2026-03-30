package com.rag.service.impl;

import com.rag.service.IRerankService;
import com.rag.service.IRetrievalService;
import com.rag.pojo.RerankResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 检索服务实现
 * <p>
 * 负责从向量数据库中检索相关文档，并使用重排序服务提高检索质量
 */
@Service
public class RetrievalServiceImpl implements IRetrievalService {

    private static final Logger logger = LoggerFactory.getLogger(RetrievalServiceImpl.class);

    private final VectorStore vectorStore;
    private final IRerankService rerankService;

    /**
     * 检索返回的文档数量
     */
    @Value("${rag.retrieval.topK:5}")
    private int topK;

    /**
     * 候选池倍数
     */
    @Value("${rag.retrieval.candidate-multiplier:2}")
    private int candidateMultiplier;

    /**
     * 构造函数，注入所需的服务
     *
     * @param vectorStore  向量存储
     * @param rerankService 重排序服务
     */
    public RetrievalServiceImpl(VectorStore vectorStore, IRerankService rerankService) {
        this.vectorStore = vectorStore;
        this.rerankService = rerankService;
    }

    /**
     * 检索相关文档
     * <p>
     * 步骤：
     * 1. 调用 vectorStore.similaritySearch 获取初始候选（扩大候选池）
     * 2. 提取文本列表传入 RerankService 进行重排序
     * 3. 返回重排序后的前 topK 个 Document
     * <p>
     * 如果 rerank 未启用或失败，则直接返回初始候选的前 topK 个
     *
     * @param query 查询文本
     * @param topK  返回的文档数量（如果为 0，则使用配置文件中的值）
     * @return 按相关性排序后的文档列表
     */
    public List<Document> retrieve(String query, int topK) {
        // 如果传入的 topK 为 0，则使用配置文件中的值
        int actualTopK = (topK > 0) ? topK : this.topK;
        logger.info("开始检索，查询: {}, topK: {}", query, actualTopK);

        try {
            // 1. 获取初始候选（扩大候选池，使用 actualTopK * candidateMultiplier）
            // 提示：用户可根据需要调整 topK 和候选池倍数（在 application.yml 中配置）
            int candidateSize = actualTopK * candidateMultiplier;
            SearchRequest candidateRequest = SearchRequest.query(query).withTopK(candidateSize);
            List<Document> initialCandidates = vectorStore.similaritySearch(candidateRequest);
            logger.info("获取到初始候选 {} 个文档", initialCandidates.size());

            // 如果初始候选为空，直接返回
            if (initialCandidates.isEmpty()) {
                return initialCandidates;
            }

            // 2. 提取文本列表
            List<String> passages = new ArrayList<>();
            for (Document doc : initialCandidates) {
                passages.add(doc.getContent());
            }

            // 3. 调用重排序服务
            List<RerankResult> rerankResults = rerankService.rerank(query, passages);
            logger.info("重排序完成，结果数量: {}", rerankResults.size());

            // 4. 构建最终结果
            List<Document> finalResults = new ArrayList<>();
            for (int i = 0; i < Math.min(actualTopK, rerankResults.size()); i++) {
                RerankResult result = rerankResults.get(i);
                int originalIndex = result.getOriginalIndex();
                if (originalIndex < initialCandidates.size()) {
                    finalResults.add(initialCandidates.get(originalIndex));
                }
            }

            logger.info("检索完成，返回 {} 个文档", finalResults.size());
            return finalResults;
        } catch (Exception e) {
            logger.error("检索失败，返回初始候选", e);
            // 降级处理，直接返回初始候选的前 actualTopK 个
            SearchRequest fallbackRequest = SearchRequest.query(query).withTopK(actualTopK);
            return vectorStore.similaritySearch(fallbackRequest);
        }
    }

}
