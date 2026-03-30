package com.rag.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 来源信息类
 */
@Data
@AllArgsConstructor
public class SourceInfo {
    private String filename;
    private int chunkIndex;
}
