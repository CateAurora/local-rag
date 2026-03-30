package com.rag;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 集成测试，验证完整的 RAG 流程
 * <p>
 * 测试步骤：
 * 1. 上传一个测试文档
 * 2. 提问相关的问题
 * 3. 断言返回的答案包含预期内容
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RagIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * 测试文档内容
     */
    private static final String TEST_DOCUMENT_CONTENT = "Spring Boot 是一个基于 Spring 框架的快速开发框架，" +
            "它可以帮助开发者快速构建生产级别的应用程序。Spring Boot 提供了自动配置、" +
            "嵌入式服务器和生产就绪的特性，使得开发者可以更加专注于业务逻辑的开发。";

    /**
     * 测试文档文件名
     */
    private static final String TEST_DOCUMENT_NAME = "sample.txt";

    /**
     * 测试问题
     */
    private static final String TEST_QUESTION = "Spring Boot 是什么？";

    /**
     * 预期答案中应包含的关键词
     */
    private static final String EXPECTED_KEYWORD = "Spring 框架";

    /**
     * 测试前的准备工作
     * <p>
     * 注意：在运行测试前，需要确保：
     * 1. Docker 已启动，且 Chroma 容器已运行
     * 2. Ollama 服务已启动，且所需模型已下载
     */
    @BeforeAll
    public static void setup() {
        System.out.println("开始集成测试，验证完整的 RAG 流程...");
        System.out.println("测试文档内容: " + TEST_DOCUMENT_CONTENT);
        System.out.println("测试问题: " + TEST_QUESTION);
    }

    /**
     * 测试完整的 RAG 流程
     * <p>
     * 步骤：
     * 1. 上传测试文档
     * 2. 提问相关问题
     * 3. 断言返回的答案包含预期内容
     */
    @Test
    public void testCompleteRagFlow() {
        // 1. 上传测试文档
        System.out.println("\n1. 上传测试文档...");
        String uploadResponse = uploadDocument(TEST_DOCUMENT_NAME, TEST_DOCUMENT_CONTENT);
        System.out.println("上传响应: " + uploadResponse);
        assertThat(uploadResponse).contains("成功");

        // 2. 等待文档处理完成（可选，根据实际情况调整）
        try {
            Thread.sleep(2000); // 等待 2 秒，确保文档已处理完成
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 3. 提问相关问题
        System.out.println("\n2. 提问相关问题...");
        Map<String, Object> response = askQuestion(TEST_QUESTION);
        System.out.println("问答响应: " + response);

        // 4. 断言返回的答案包含预期内容
        System.out.println("\n3. 验证答案...");
        String answer = (String) response.get("answer");
        assertThat(answer).contains(EXPECTED_KEYWORD);
        System.out.println("答案验证成功，包含预期关键词: " + EXPECTED_KEYWORD);
    }

    /**
     * 上传文档
     *
     * @param filename 文件名
     * @param content  文件内容
     * @return 上传响应
     */
    private String uploadDocument(String filename, String content) {
        // 创建文件内容
        ByteArrayResource resource = new ByteArrayResource(content.getBytes()) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        // 构建请求参数
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // 构建请求实体
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // 发送请求
        ResponseEntity<String> response = restTemplate.postForEntity("/documents/upload", requestEntity, String.class);
        return response.getBody();
    }

    /**
     * 提问问题
     *
     * @param question 问题
     * @return 问答响应
     */
    private Map<String, Object> askQuestion(String question) {
        // 构建请求参数
        Map<String, String> request = new HashMap<>();
        request.put("question", question);

        // 发送请求
        ResponseEntity<Map> response = restTemplate.postForEntity("/chat/ask", request, Map.class);
        return response.getBody();
    }

}
