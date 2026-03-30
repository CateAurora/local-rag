package com.rag.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 重排序结果类
 */
@Data
@AllArgsConstructor
public class RerankResult {
    private String text;
    private double score;
    private int originalIndex;
}
