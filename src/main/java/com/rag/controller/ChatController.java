package com.rag.controller;

import com.rag.service.IAgentService;
import com.rag.service.IRagService;
import com.rag.pojo.AgentResult;
import com.rag.pojo.RagResult;
import com.rag.pojo.SourceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天控制器
 * <p>
 * 提供 RAG 问答接口和 Agent 工具调用接口
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final IRagService ragService;
    private final IAgentService agentService;

    /**
     * 构造函数，注入所需的服务
     *
     * @param ragService   RAG 服务
     * @param agentService Agent 服务
     */
    public ChatController(IRagService ragService, IAgentService agentService) {
        this.ragService = ragService;
        this.agentService = agentService;
    }

    /**
     * 问答接口
     * <p>
     * 接收问题，返回答案和来源
     * <p>
     * 测试方法：
     * curl -X POST http://localhost:8080/chat/ask -H "Content-Type: application/json" -d "{\"question\":\"你的问题\"}"
     *
     * @param request 请求体，包含 question 字段
     * @return 包含答案和来源的响应
     */
    @PostMapping("/ask")
    public ResponseEntity<?> ask(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        if (question == null || question.isEmpty()) {
            return ResponseEntity.badRequest().body("缺少 question 参数");
        }

        logger.info("接收到问答请求: {}", question);

        try {
            // 调用 RagService 处理问题
            RagResult result = ragService.ask(question);

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("answer", result.getAnswer());

            // 构建 sources 列表
            List<Map<String, Object>> sources = new ArrayList<>();
            for (SourceInfo source : result.getSources()) {
                Map<String, Object> sourceMap = new HashMap<>();
                sourceMap.put("filename", source.getFilename());
                sourceMap.put("chunkIndex", source.getChunkIndex());
                sources.add(sourceMap);
            }
            response.put("sources", sources);

            // 添加耗时信息（可选）
            response.put("retrievalTime", result.getRetrievalTime());
            response.put("generationTime", result.getGenerationTime());
            response.put("totalTime", result.getTotalTime());

            logger.info("问答完成，返回答案和来源");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("问答失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("问答失败: " + e.getMessage());
        }
    }

    /**
     * Agent 工具调用接口
     * <p>
     * 接收问题和历史消息，返回带工具调用的结果
     * <p>
     * 测试方法：
     * curl -X POST http://localhost:8080/chat/agent -H "Content-Type: application/json" -d "{\"question\":\"帮我搜索包含 spring 的笔记文件\", \"history\":[]}"
     *
     * @param request 请求体，包含 question 和 history 字段
     * @return 包含答案、历史消息和工具调用状态的响应
     */
    @PostMapping("/agent")
    public ResponseEntity<?> agent(@RequestBody Map<String, Object> request) {
        String question = (String) request.get("question");
        if (question == null || question.isEmpty()) {
            return ResponseEntity.badRequest().body("缺少 question 参数");
        }

        // 获取历史消息
        List<Map<String, String>> history = new ArrayList<>();
        if (request.containsKey("history")) {
            Object historyObj = request.get("history");
            if (historyObj instanceof List) {
                for (Object item : (List<?>) historyObj) {
                    if (item instanceof Map) {
                        Map<?, ?> itemMap = (Map<?, ?>) item;
                        Map<String, String> historyItem = new HashMap<>();
                        if (itemMap.containsKey("user")) {
                            historyItem.put("user", itemMap.get("user").toString());
                        }
                        if (itemMap.containsKey("assistant")) {
                            historyItem.put("assistant", itemMap.get("assistant").toString());
                        }
                        if (!historyItem.isEmpty()) {
                            history.add(historyItem);
                        }
                    }
                }
            }
        }

        logger.info("接收到 Agent 请求: {}", question);

        try {
            // 调用 AgentService 处理问题
            AgentResult result = agentService.chat(question, history);

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("answer", result.getAnswer());
            response.put("history", result.getHistory());
            response.put("toolCalled", result.isToolCalled());

            logger.info("Agent 处理完成，返回结果");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Agent 处理失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Agent 处理失败: " + e.getMessage());
        }
    }
}
