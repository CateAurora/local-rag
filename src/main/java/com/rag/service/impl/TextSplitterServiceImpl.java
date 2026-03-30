package com.rag.service.impl;

import com.rag.service.ITextSplitterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本分块服务实现
 * <p>
 * 将长文本分割成多个块，便于向量化存储
 */
@Service
public class TextSplitterServiceImpl implements ITextSplitterService {

    private static final Logger logger = LoggerFactory.getLogger(TextSplitterServiceImpl.class);

    /**
     * 分块大小，默认为 500 字符
     * 可在 application.yml 中配置
     */
    @Value("${rag.text-splitter.chunk-size:500}")
    private int chunkSize;

    /**
     * 重叠大小，默认为 50 字符
     * 可在 application.yml 中配置
     */
    @Value("${rag.text-splitter.overlap:50}")
    private int overlap;

    /**
     * 分割文本，返回文本块列表
     * <p>
     * 实现递归字符分割，保留原文本的结构
     *
     * @param text 要分割的文本
     * @return 分割后的文本块列表
     */
    public List<String> split(String text) {
        logger.info("开始分割文本，总长度: {}", text.length());
        List<String> chunks = new ArrayList<>();
        splitRecursive(text, chunks);
        logger.info("文本分割完成，共生成 {} 个块", chunks.size());
        return chunks;
    }

    /**
     * 递归分割文本
     * <p>
     * 1. 首先按段落分割
     * 2. 对每个段落，如果长度超过 chunkSize，则进一步分割
     * 3. 确保分割后的块有一定的重叠，以保持上下文
     *
     * @param text   要分割的文本
     * @param chunks 存储分割结果的列表
     */
    private void splitRecursive(String text, List<String> chunks) {
        // 按段落分割
        String[] paragraphs = text.split("\\n\\s*\\n");

        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty()) {
                continue;
            }

            if (paragraph.length() <= chunkSize) {
                // 段落长度小于等于 chunkSize，直接添加
                chunks.add(paragraph);
            } else {
                // 段落长度大于 chunkSize，进一步分割
                splitParagraph(paragraph, chunks);
            }
        }
    }

    /**
     * 分割单个段落
     * <p>
     * 按 chunkSize 分割段落，确保有 overlap 重叠
     *
     * @param paragraph 要分割的段落
     * @param chunks    存储分割结果的列表
     */
    private void splitParagraph(String paragraph, List<String> chunks) {
        int start = 0;
        int length = paragraph.length();

        while (start < length) {
            int end = Math.min(start + chunkSize, length);

            // 尝试在单词边界分割
            if (end < length) {
                // 找到最后一个空格或标点
                int lastSpace = paragraph.lastIndexOf(' ', end);
                if (lastSpace > start) {
                    end = lastSpace;
                }
            }

            // 添加当前块
            chunks.add(paragraph.substring(start, end).trim());

            // 计算下一个块的起始位置，考虑重叠
            start = end - overlap;
            if (start < 0) {
                start = 0;
            }

            // 避免无限循环
            if (start >= end) {
                start = end;
            }
        }
    }

}
