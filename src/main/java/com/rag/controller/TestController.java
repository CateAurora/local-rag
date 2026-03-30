package com.rag.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器
 * <p>
 * 用于验证 RAG 系统是否正常启动
 */
@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * 测试接口，返回 "RAG system is running"
     *
     * @return 测试响应信息
     */
    @GetMapping("/status")
    public String getStatus() {
        return "RAG system is running";
    }

}
