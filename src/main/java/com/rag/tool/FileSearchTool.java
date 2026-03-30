package com.rag.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件搜索工具
 * <p>
 * 用于搜索本地文件系统中的文件
 */
@Component
public class FileSearchTool {

    private static final Logger logger = LoggerFactory.getLogger(FileSearchTool.class);

    /**
     * 搜索文件
     * <p>
     * 基于本地文件系统扫描，返回匹配的文件名列表
     * 支持通配符或简单关键词匹配
     *
     * @param keyword 搜索关键词
     * @return 匹配的文件名列表
     */
    public List<String> searchFiles(String keyword) {
        logger.info("开始搜索文件，关键词: {}", keyword);

        List<String> result = new ArrayList<>();

        // 搜索当前工作目录
        File currentDir = new File(System.getProperty("user.dir"));
        searchDirectory(currentDir, keyword, result);

        logger.info("搜索完成，找到 {} 个匹配文件", result.size());
        return result;
    }

    /**
     * 递归搜索目录
     *
     * @param directory 目录
     * @param keyword   搜索关键词
     * @param result    结果列表
     */
    private void searchDirectory(File directory, String keyword, List<String> result) {
        // 检查目录是否存在
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }

        // 获取目录中的文件和子目录
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isFile()) {
                // 检查文件名是否匹配关键词
                if (file.getName().toLowerCase().contains(keyword.toLowerCase())) {
                    result.add(file.getAbsolutePath());
                }
            } else if (file.isDirectory()) {
                // 递归搜索子目录
                searchDirectory(file, keyword, result);
            }
        }
    }

    /**
     * 获取工具描述
     * <p>
     * 用于向模型描述工具的功能和参数
     *
     * @return 工具描述
     */
    public String getToolDescription() {
        return "searchFiles: 搜索本地文件系统中的文件\n" +
               "参数: keyword (字符串) - 搜索关键词\n" +
               "返回值: 匹配的文件名列表\n" +
               "用途: 当用户需要查找特定文件时使用此工具";
    }

}
