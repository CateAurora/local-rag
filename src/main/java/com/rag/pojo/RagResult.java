package com.rag.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * RAG 结果类
 */
@Data
@AllArgsConstructor
public class RagResult {
    private String answer;
    private List<SourceInfo> sources;
    private long retrievalTime;
    private long generationTime;
    private long totalTime;
}
