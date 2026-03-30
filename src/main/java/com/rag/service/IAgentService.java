package com.rag.service;

import com.rag.pojo.AgentResult;

import java.util.List;
import java.util.Map;

/**
 * Agent 服务接口
 * <p>
 * 定义对话式问答的核心功能
 */
public interface IAgentService {

    /**
     * 处理用户问题
     *
     * @param question 问题
     * @param history  历史消息列表
     * @return 处理结果
     */
    AgentResult chat(String question, List<Map<String, String>> history);

}
