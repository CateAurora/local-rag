package com.rag.service;

import java.util.List;

/**
 * 文本分块服务接口
 * <p>
 * 定义文本分块的核心功能
 */
public interface ITextSplitterService {

    /**
     * 将文本分割成多个块
     *
     * @param text 要分割的文本
     * @return 分割后的文本块列表
     */
    List<String> split(String text);

}
