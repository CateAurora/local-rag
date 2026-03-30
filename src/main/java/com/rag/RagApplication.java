package com.rag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 本地 RAG 系统主启动类
 * <p>
 * 人工需要执行的步骤：
 * 1. 确认 JDK 17+ 已安装
 * 2. 确认 Docker 已启动，且 Chroma 容器已运行（执行命令：docker run -d -p 8000:8000 chromadb/chroma）
 * 3. 确认 Ollama 服务已启动（ollama serve）
 * 4. 如何通过 Maven 编译运行（mvn spring-boot:run）
 */
@SpringBootApplication
public class RagApplication {

    /**
     * 主方法，启动 Spring Boot 应用
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(RagApplication.class, args);
    }

}
