package com.rag.service;

import java.io.File;

/**
 * 文档摄入服务接口
 * <p>
 * 定义文档摄入的核心功能
 */
public interface IIngestionService {

    /**
     * 摄入文档到向量数据库
     *
     * @param file 要摄入的文件
     * @return 成功存储的块数
     * @throws Exception 当处理过程中发生错误时抛出
     */
    int ingestDocument(File file) throws Exception;

}
