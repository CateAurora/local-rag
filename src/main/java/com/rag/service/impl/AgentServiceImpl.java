package com.rag.service.impl;

import com.rag.service.IAgentService;
import com.rag.tool.FileSearchTool;
import com.rag.pojo.AgentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Agent 服务实现
 * <p>
 * 实现对话式问答，当用户问题需要搜索文件时，自动调用工具
 */
@Service
public class AgentServiceImpl implements IAgentService {

    private static final Logger logger = LoggerFactory.getLogger(AgentServiceImpl.class);

    private final ChatModel chatModel;
    private final FileSearchTool fileSearchTool;

    /**
     * 是否启用工具调用
     */
    @Value("${rag.agent.function-calling.enabled:false}")
    private boolean functionCallingEnabled;

    /**
     * 构造函数，注入所需的服务和模型
     *
     * @param chatModel      聊天模型
     * @param fileSearchTool 文件搜索工具
     */
    public AgentServiceImpl(ChatModel chatModel, FileSearchTool fileSearchTool) {
        this.chatModel = chatModel;
        this.fileSearchTool = fileSearchTool;
    }

    /**
     * 处理用户问题
     * <p>
     * 支持多轮对话，当用户问题需要搜索文件时，自动调用工具
     *
     * @param question 问题
     * @param history  历史消息列表
     * @return 处理结果
     */
    public AgentResult chat(String question, List<Map<String, String>> history) {
        logger.info("开始处理问题: {}", question);

        // 构建对话历史
        StringBuilder historyBuilder = new StringBuilder();
        if (history != null && !history.isEmpty()) {
            for (Map<String, String> message : history) {
                if (message.containsKey("user")) {
                    historyBuilder.append("用户: " + message.get("user") + "\n");
                }
                if (message.containsKey("assistant")) {
                    historyBuilder.append("助手: " + message.get("assistant") + "\n");
                }
            }
        }

        // 构建工具描述
        String toolsDescription = "\n\n可用工具:\n" + fileSearchTool.getToolDescription();

        // 构建提示词
        String prompt = String.format("%s\n用户: %s\n\n请根据问题和对话历史回答。如果需要搜索文件，请使用 searchFiles 工具。\n" +
                "使用工具的格式：\n" +
                "工具调用: searchFiles\n" +
                "参数: {\"keyword\": \"搜索关键词\"}\n" +
                "\n" +
                "如果不需要使用工具，请直接回答。%s", 
                historyBuilder.toString(), question, functionCallingEnabled ? toolsDescription : "");

        logger.debug("构建的 prompt: {}", prompt);

        // 调用模型
        ChatResponse response = chatModel.call(new Prompt(new UserMessage(prompt)));
        String modelOutput = response.getResult().getOutput().getContent();
        logger.info("模型输出: {}", modelOutput);

        // 检查是否需要调用工具
        if (functionCallingEnabled && modelOutput.contains("工具调用:")) {
            return handleToolCall(modelOutput, question, history);
        } else {
            // 直接返回模型的回答
            List<Map<String, String>> newHistory = new ArrayList<>(history != null ? history : List.of());
            Map<String, String> userMessage = Map.of("user", question);
            Map<String, String> assistantMessage = Map.of("assistant", modelOutput);
            newHistory.add(userMessage);
            newHistory.add(assistantMessage);

            return new AgentResult(modelOutput, newHistory, false);
        }
    }

    /**
     * 处理工具调用
     * <p>
     * 解析模型输出的工具调用指令，执行工具，然后将结果返回给模型
     *
     * @param modelOutput 模型输出
     * @param question    用户问题
     * @param history     历史消息
     * @return 处理结果
     */
    private AgentResult handleToolCall(String modelOutput, String question, List<Map<String, String>> history) {
        // 解析工具调用
        Pattern toolPattern = Pattern.compile("工具调用: (\\w+)");
        Matcher toolMatcher = toolPattern.matcher(modelOutput);

        if (toolMatcher.find()) {
            String toolName = toolMatcher.group(1);
            logger.info("检测到工具调用: {}", toolName);

            // 解析参数
            Pattern paramPattern = Pattern.compile("参数: (\\{[^}]+\\})");
            Matcher paramMatcher = paramPattern.matcher(modelOutput);

            if (paramMatcher.find()) {
                String paramJson = paramMatcher.group(1);
                logger.info("工具参数: {}", paramJson);

                // 执行工具调用
                if ("searchFiles".equals(toolName)) {
                    // 提取关键词
                    String keyword = extractKeywordFromJson(paramJson);
                    if (keyword != null) {
                        // 执行文件搜索
                        List<String> files = fileSearchTool.searchFiles(keyword);
                        logger.info("文件搜索结果: {}", files);

                        // 将工具执行结果返回给模型
                        return callModelWithToolResult(question, history, toolName, keyword, files);
                    }
                }
            }
        }

        // 如果解析失败，直接返回模型的输出
        List<Map<String, String>> newHistory = new ArrayList<>(history != null ? history : List.of());
        Map<String, String> userMessage = Map.of("user", question);
        Map<String, String> assistantMessage = Map.of("assistant", modelOutput);
        newHistory.add(userMessage);
        newHistory.add(assistantMessage);

        return new AgentResult(modelOutput, newHistory, false);
    }

    /**
     * 从 JSON 字符串中提取关键词
     *
     * @param json JSON 字符串
     * @return 关键词
     */
    private String extractKeywordFromJson(String json) {
        // 简单的 JSON 解析，实际项目中可使用 JSON 库
        Pattern keywordPattern = Pattern.compile("\"keyword\":\\s*\"([^\"]+)\"");
        Matcher matcher = keywordPattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 将工具执行结果返回给模型
     *
     * @param question    用户问题
     * @param history     历史消息
     * @param toolName    工具名称
     * @param keyword     搜索关键词
     * @param files       搜索结果
     * @return 处理结果
     */
    private AgentResult callModelWithToolResult(String question, List<Map<String, String>> history, 
                                              String toolName, String keyword, List<String> files) {
        // 构建工具执行结果
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("工具执行结果:\n");
        resultBuilder.append("工具: " + toolName + "\n");
        resultBuilder.append("参数: {\"keyword\": \"" + keyword + "\"}\n");
        resultBuilder.append("结果: \n");
        if (files.isEmpty()) {
            resultBuilder.append("未找到匹配的文件\n");
        } else {
            for (String file : files) {
                resultBuilder.append("- " + file + "\n");
            }
        }

        // 构建对话历史
        StringBuilder historyBuilder = new StringBuilder();
        if (history != null && !history.isEmpty()) {
            for (Map<String, String> message : history) {
                if (message.containsKey("user")) {
                    historyBuilder.append("用户: " + message.get("user") + "\n");
                }
                if (message.containsKey("assistant")) {
                    historyBuilder.append("助手: " + message.get("assistant") + "\n");
                }
            }
        }

        // 构建提示词
        String prompt = String.format("%s\n用户: %s\n\n%s\n\n请根据工具执行结果回答用户的问题。", 
                historyBuilder.toString(), question, resultBuilder.toString());

        logger.debug("构建的工具结果 prompt: {}", prompt);

        // 调用模型
        ChatResponse response = chatModel.call(new Prompt(new UserMessage(prompt)));
        String modelOutput = response.getResult().getOutput().getContent();
        logger.info("模型基于工具结果的输出: {}", modelOutput);

        // 更新历史消息
        List<Map<String, String>> newHistory = new ArrayList<>(history != null ? history : List.of());
        Map<String, String> userMessage = Map.of("user", question);
        Map<String, String> toolCallMessage = Map.of("assistant", "工具调用: " + toolName + "\n参数: {\"keyword\": \"" + keyword + "\"}");
        Map<String, String> toolResultMessage = Map.of("assistant", "工具执行结果: \n" + resultBuilder.toString());
        Map<String, String> assistantMessage = Map.of("assistant", modelOutput);
        newHistory.add(userMessage);
        newHistory.add(toolCallMessage);
        newHistory.add(toolResultMessage);
        newHistory.add(assistantMessage);

        return new AgentResult(modelOutput, newHistory, true);
    }


}
