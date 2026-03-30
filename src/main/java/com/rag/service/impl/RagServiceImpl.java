package com.rag.service.impl;

import com.rag.service.IRagService;
import com.rag.service.IRetrievalService;
import com.rag.pojo.RagResult;
import com.rag.pojo.SourceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * RAG 服务实现
 * <p>
 * 实现完整的 RAG 问答流程：检索相关文档 → 构建上下文 → 生成答案
 */
@Service
public class RagServiceImpl implements IRagService {

    private static final Logger logger = LoggerFactory.getLogger(RagServiceImpl.class);

    private final IRetrievalService retrievalService;
    private final ChatModel chatModel;

    /**
     * Prompt 模板
     */
    @Value("${rag.rag.prompt-template:基于以下内容回答问题：\n%s\n\n问题：%s\n答案：}")
    private String promptTemplate;

    /**
     * 检索返回的文档数量
     */
    @Value("${rag.retrieval.topK:5}")
    private int topK;

    /**
     * 构造函数，注入所需的服务和模型
     *
     * @param retrievalService 检索服务
     * @param chatModel        聊天模型（Spring AI 的 OllamaChatModel）
     */
    public RagServiceImpl(IRetrievalService retrievalService, ChatModel chatModel) {
        this.retrievalService = retrievalService;
        this.chatModel = chatModel;
    }

    /**
     * 回答问题
     * <p>
     * 步骤：
     * 1. 调用 retrievalService.retrieve 获取上下文
     * 2. 构建 prompt
     * 3. 调用 chatModel.call 获取答案
     * 4. 返回答案及使用的上下文
     *
     * @param question 问题
     * @return RAG 结果，包含答案和来源
     */
    public RagResult ask(String question) {
        logger.info("开始处理问题: {}", question);

        long startTime = System.currentTimeMillis();
        List<SourceInfo> sources = new ArrayList<>();

        try {
            // 1. 调用 retrievalService.retrieve 获取上下文（使用配置文件中的 topK）
            long retrievalStart = System.currentTimeMillis();
            List<Document> documents = retrievalService.retrieve(question, topK);
            long retrievalEnd = System.currentTimeMillis();
            long retrievalTime = retrievalEnd - retrievalStart;
            logger.info("检索耗时: {}ms，获取到 {} 个文档", retrievalTime, documents.size());

            // 2. 构建上下文
            StringBuilder contextBuilder = new StringBuilder();

            for (int i = 0; i < documents.size(); i++) {
                Document doc = documents.get(i);
                contextBuilder.append("[").append(i + 1).append("] ").append(doc.getContent()).append("\n\n");

                // 提取来源信息
                sources.add(new SourceInfo(doc.getMetadata().get("filename").toString(), Integer.parseInt(doc.getMetadata().get("chunkIndex").toString())));
            }

            String context = contextBuilder.toString();
            // 如果上下文过长，可添加截断逻辑
            // 例如：if (context.length() > MAX_CONTEXT_LENGTH) { context = context.substring(0, MAX_CONTEXT_LENGTH); }

            // 3. 构建 prompt（使用配置文件中的模板）
            // Prompt 模板可根据实际效果调整（在 application.yml 中配置）
            String prompt = String.format(promptTemplate, context, question);
            logger.debug("构建的 prompt: {}", prompt);

            // 4. 调用 chatModel.call 获取答案
            long generationStart = System.currentTimeMillis();
            ChatResponse response = chatModel.call(new Prompt(new UserMessage(prompt)));
            long generationEnd = System.currentTimeMillis();
            long generationTime = generationEnd - generationStart;
            logger.info("生成耗时: {}ms", generationTime);

            String answer = response.getResult().getOutput().getContent();
            logger.info("生成的答案: {}", answer);

            long totalTime = System.currentTimeMillis() - startTime;
            logger.info("总耗时: {}ms", totalTime);

            // 5. 返回答案及使用的上下文
            return new RagResult(answer, sources, retrievalTime, generationTime, totalTime);
        } catch (Exception e) {
            logger.error("RAG 处理失败: {}", e.getMessage(), e);
            // 降级处理，返回错误信息
            long totalTime = System.currentTimeMillis() - startTime;
            return new RagResult("抱歉，处理您的问题时出错，请稍后再试。", sources, 0, 0, totalTime);
        }
    }
}
