package com.hupi.hupiaiagent.rag;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自定义基于阿里云知识库服务的 RAG 增强顾问
 * 注意：使用Ollama时，此配置暂时禁用，因为需要DashScope API Key
 */
@Configuration
@Slf4j
public class DevOpsAppRagCloudAdvisorConfig {

    @Value("${spring.ai.dashscope.api-key:}")
    private String dashScopeApiKey;

    @Bean
    public Advisor devOpsAppRagCloudAdvisor() {
        // 如果没有配置DashScope API Key，返回null，禁用RAG功能
        if (dashScopeApiKey == null || dashScopeApiKey.trim().isEmpty()) {
            log.warn("DashScope API Key not configured, RAG cloud advisor is disabled");
            return null;
        }
        
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(dashScopeApiKey)
                .build();
        final String KNOWLEDGE_INDEX = "AI运维知识库";
        DocumentRetriever dashScopeDocumentRetriever = new DashScopeDocumentRetriever(dashScopeApi,
                DashScopeDocumentRetrieverOptions.builder()
                        .withIndexName(KNOWLEDGE_INDEX)
                        .build());
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(dashScopeDocumentRetriever)
                .build();
    }
}
