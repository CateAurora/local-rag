package com.rag.service.impl;

import com.rag.service.IDocumentParserService;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * 文档解析服务实现
 * <p>
 * 使用 Apache Tika 解析不同类型的文件，返回纯文本
 * 支持的文件类型：txt、md、pdf、docx 等
 */
@Service
public class DocumentParserServiceImpl implements IDocumentParserService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentParserServiceImpl.class);
    private final Tika tika;

    /**
     * 构造函数，初始化 Tika 实例
     */
    public DocumentParserServiceImpl() {
        // 创建 Tika 实例，用于自动检测文件类型并解析
        this.tika = new Tika();
    }

    /**
     * 解析文件，返回纯文本
     * <p>
     * Tika 会自动识别文件类型并使用相应的解析器
     *
     * @param file 要解析的文件
     * @return 解析后的纯文本
     * @throws IOException 当文件读取失败时抛出
     * @throws TikaException 当文件解析失败时抛出
     */
    public String parse(File file) throws IOException, TikaException {
        try {
            logger.info("开始解析文件: {}", file.getName());
            // Tika 自动检测文件类型并解析
            String content = tika.parseToString(file);
            logger.info("文件解析完成: {}, 内容长度: {}", file.getName(), content.length());
            return content;
        } catch (IOException | TikaException e) {
            logger.error("文件解析失败: {}", file.getName(), e);
            throw e;
        }
    }

}
