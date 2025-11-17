package com.hupi.hupiaiagent.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * ChatModel 配置类
 * 将 Spring AI 自动配置的 ollamaChatModel 注册为 dashscopeChatModel
 */
@Configuration
public class ChatModelConfig {

    /**
     * 创建 dashscopeChatModel Bean
     * 使用 Spring AI 自动配置的 ollamaChatModel
     */
    @Bean("dashscopeChatModel")
    @Primary
    public ChatModel dashscopeChatModel(@Autowired(required = false) List<ChatModel> chatModels) {
        // 如果没有自动配置的 chatModels，返回 null
        if (chatModels == null || chatModels.isEmpty()) {
            throw new IllegalStateException("No ChatModel found. Please ensure Ollama is properly configured.");
        }
        
        // 返回找到的第一个 ChatModel
        return chatModels.get(0);
    }
}

