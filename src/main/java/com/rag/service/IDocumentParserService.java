package com.rag.service;

import java.io.File;
import java.io.IOException;

/**
 * 文档解析服务接口
 * <p>
 * 定义文档解析的核心功能
 */
public interface IDocumentParserService {

    /**
     * 解析文件，返回纯文本
     *
     * @param file 要解析的文件
     * @return 解析后的纯文本
     * @throws IOException 当文件读取失败时抛出
     * @throws org.apache.tika.exception.TikaException 当文件解析失败时抛出
     */
    String parse(File file) throws IOException, org.apache.tika.exception.TikaException;

}
