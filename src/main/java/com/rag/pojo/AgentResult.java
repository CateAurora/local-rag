package com.rag.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Agent 结果类
 */
@Data
@AllArgsConstructor
public class AgentResult {
    private String answer;
    private List<Map<String, String>> history;
    private boolean toolCalled;
}
