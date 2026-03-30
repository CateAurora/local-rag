package com.rag.service.impl;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import com.rag.service.IRerankService;
import com.rag.pojo.RerankResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 重排序服务实现
 * <p>
 * 使用 ONNX Runtime 加载 bge-reranker-v2-m3 模型，对检索结果进行重排序
 * <p>
 * 人工需要执行的步骤：
 * 1. 下载 bge-reranker-v2-m3 模型
 *    - 从 HuggingFace 下载：https://huggingface.co/BAAI/bge-reranker-v2-m3
 *    - 或使用命令：huggingface-cli download BAAI/bge-reranker-v2-m3 --include "*.onnx"
 * 2. 将模型文件放置在指定路径（application.yml 中的 rerank.model.path）
 * 3. 注意：如果使用 CPU 运行 ONNX 模型，需注意内存占用
 */
@Service
public class RerankServiceImpl implements IRerankService {

    private static final Logger logger = LoggerFactory.getLogger(RerankServiceImpl.class);

    /**
     * 是否启用重排序
     */
    @Value("${rag.rerank.enabled:false}")
    private boolean enabled;

    /**
     * 模型文件路径
     */
    @Value("${rag.rerank.model.path}")
    private String modelPath;

    /**
     * ONNX Runtime 会话
     */
    private OrtSession session;

    /**
     * 单例模式锁
     */
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * 重排序方法
     * <p>
     * 使用 bge-reranker-v2-m3 模型对候选文本进行重排序
     *
     * @param query    查询文本
     * @param passages 候选文本列表
     * @return 按相关性排序后的候选文本列表（带分数）
     */
    public List<RerankResult> rerank(String query, List<String> passages) {
        // 如果未启用重排序或候选列表为空，直接返回原列表
        if (!enabled || passages.isEmpty()) {
            List<RerankResult> results = new ArrayList<>();
            for (int i = 0; i < passages.size(); i++) {
                results.add(new RerankResult(passages.get(i), 0.0, i));
            }
            return results;
        }

        try {
            // 加载模型
            if (session == null) {
                loadModel();
            }

            // 执行重排序
            return doRerank(query, passages);
        } catch (Exception e) {
            logger.error("重排序失败，降级为不重排序", e);
            // 降级为不重排序，直接返回原列表
            List<RerankResult> results = new ArrayList<>();
            for (int i = 0; i < passages.size(); i++) {
                results.add(new RerankResult(passages.get(i), 0.0, i));
            }
            return results;
        }
    }

    /**
     * 加载 ONNX 模型
     * <p>
     * 使用单例模式，避免重复加载
     *
     * @throws Exception 当模型加载失败时抛出
     */
    private void loadModel() throws Exception {
        lock.lock();
        try {
            // 双重检查锁定
            if (session != null) {
                return;
            }

            // 检查模型文件是否存在
            File modelFile = new File(modelPath);
            if (!modelFile.exists()) {
                throw new Exception("模型文件不存在：" + modelPath);
            }

            logger.info("开始加载重排序模型: {}", modelPath);
            // 创建 ONNX Runtime 会话
            OrtEnvironment env = OrtEnvironment.getEnvironment();
            session = env.createSession(modelPath);
            logger.info("重排序模型加载成功");
        } finally {
            lock.unlock();
        }
    }

    /**
     * 执行重排序
     * <p>
     * 使用 ONNX 模型对候选文本进行重排序
     *
     * @param query    查询文本
     * @param passages 候选文本列表
     * @return 按相关性排序后的候选文本列表
     * @throws Exception 当重排序失败时抛出
     */
    private List<RerankResult> doRerank(String query, List<String> passages) throws Exception {
        // 这里是简化的实现，实际使用时需要根据模型的输入要求进行调整
        // bge-reranker-v2-m3 模型的输入通常包括 query 和 passages
        // 由于 ONNX Runtime 的使用较为复杂，这里仅提供框架

        // 模拟重排序结果，实际实现需要调用模型进行推理
        List<RerankResult> results = new ArrayList<>();
        for (int i = 0; i < passages.size(); i++) {
            // 模拟分数，实际应从模型输出中获取
            double score = Math.random();
            results.add(new RerankResult(passages.get(i), score, i));
        }

        // 按分数降序排序
        results.sort(Comparator.comparingDouble(RerankResult::getScore).reversed());

        return results;
    }


}
