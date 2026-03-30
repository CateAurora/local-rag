# 本地 RAG 系统

## 项目简介

本项目是一个基于 Spring Boot 3.2.x 的本地 RAG（Retrieval-Augmented Generation）系统，使用 Ollama 作为 LLM 服务，Chroma 作为向量数据库，实现了文档上传、检索、重排序和问答功能。

## 功能特性

- 文档上传与解析（支持 txt、md、pdf、docx 等格式）
- 文本分块与向量化
- 向量检索与重排序
- RAG 问答
- Agent 工具调用（文件搜索）

## 环境要求

- JDK 17+
- Docker
- Maven
- Ollama

## 前置步骤

### 1. 安装 JDK 17+

确保你的系统已安装 JDK 17 或更高版本。

### 2. 启动 Chroma 容器

使用 Docker 运行 Chroma 容器：

```bash
docker run -d -p 8000:8000 chromadb/chroma
```

### 3. 安装并启动 Ollama

1. 从 [Ollama 官网](https://ollama.ai/) 下载并安装 Ollama
2. 启动 Ollama 服务：

   ```bash
   ollama serve
   ```

3. 下载所需模型：

   ```bash
   # 对话模型
   ollama pull qwen2.5:7b
   
   # 嵌入模型
   ollama pull nomic-embed-text
   ```

### 4. 下载重排序模型（可选）

如果需要使用重排序功能，需要下载 bge-reranker-v2-m3 模型：

```bash
huggingface-cli download BAAI/bge-reranker-v2-m3 --include "*.onnx"
```

将下载的模型文件放置在 `C:/models/bge-reranker-v2-m3/model.onnx`（或修改 `application.yml` 中的路径）。

## 项目配置

### 配置文件

项目的主要配置在 `src/main/resources/application.yml` 文件中，包括：

- Ollama 服务配置
- Chroma 向量数据库配置
- 文本分块配置
- 重排序配置
- 检索配置
- Agent 工具调用配置

### 关键配置项

```yaml
# Ollama 配置
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        model: qwen2.5:7b
      embedding:
        model: nomic-embed-text

# Chroma 配置
    vectorstore:
      chroma:
        base-url: http://localhost:8000
        collection-name: rag_docs

# 文本分块配置
rag:
  text-splitter:
    chunk-size: 500
    overlap: 50

# 检索配置
  retrieval:
    topK: 5
    candidate-multiplier: 2

# 重排序配置
  rerank:
    enabled: true
    model:
      path: C:/models/bge-reranker-v2-m3/model.onnx

# Agent 工具调用配置
  agent:
    function-calling:
      enabled: true
```

## 运行项目

### 编译项目

```bash
mvn clean package
```

### 启动应用

```bash
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动。

## 测试接口

### 1. 上传文档

使用 curl 上传文档：

```bash
curl -X POST -F "file=@/path/to/document.pdf" http://localhost:8080/documents/upload
```

### 2. RAG 问答

使用 curl 进行问答：

```bash
curl -X POST http://localhost:8080/chat/ask -H "Content-Type: application/json" -d "{\"question\":\"你的问题\"}"
```

### 3. Agent 工具调用

使用 curl 测试 Agent 工具调用：

```bash
curl -X POST http://localhost:8080/chat/agent -H "Content-Type: application/json" -d "{\"question\":\"帮我搜索包含 spring 的文件\", \"history\":[]}"
```

## 集成测试

项目包含一个集成测试 `RagIntegrationTest.java`，用于验证完整的 RAG 流程：

1. 上传测试文档
2. 提问相关问题
3. 断言返回的答案包含预期内容

运行测试：

```bash
mvn test -Dtest=RagIntegrationTest
```

## 项目结构

```
local-rag/
├── src/
│   ├── main/
│   │   ├── java/com/rag/
│   │   │   ├── config/          # 配置类
│   │   │   ├── controller/       # 控制器
│   │   │   ├── service/          # 服务层
│   │   │   ├── tool/             # 工具类
│   │   │   └── RagApplication.java  # 主启动类
│   │   └── resources/
│   │       └── application.yml   # 配置文件
│   └── test/
│       └── java/com/rag/         # 测试类
├── pom.xml                        # Maven 依赖
└── README.md                      # 项目说明
```

## 核心服务

1. **DocumentParserService**：使用 Apache Tika 解析文件
2. **TextSplitterService**：实现文本分块
3. **IngestionService**：文档摄入与向量化
4. **RetrievalService**：向量检索与重排序
5. **RerankService**：使用 ONNX 模型进行重排序
6. **RagService**：RAG 问答流程
7. **AgentService**：Agent 工具调用

## 注意事项

1. 确保 Chroma 容器和 Ollama 服务已启动
2. 确保所需模型已下载
3. 注意文件大小限制（默认 10MB）
4. 重排序模型较大，使用 CPU 运行时需注意内存占用
5. 可根据实际需求调整配置参数

## 故障排查

1. **服务连接失败**：检查 Ollama 和 Chroma 服务是否正常运行
2. **模型未找到**：确保已下载所需模型
3. **内存不足**：调整 JVM 内存参数或减少 batch size
4. **文件解析失败**：检查文件格式是否支持，或文件是否损坏

