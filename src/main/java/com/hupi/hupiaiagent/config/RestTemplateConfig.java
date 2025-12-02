package com.hupi.hupiaiagent.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate 配置类
 * 为不同的服务配置专用的 RestTemplate 实例
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 通用 RestTemplate Bean
     * 用于常规的 HTTP 请求，包括 TTS 服务调用
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(30))  // 连接超时：30秒
                .setReadTimeout(Duration.ofMinutes(10))      // 读取超时：10分钟（适应TTS生成时间）
                .build();
    }

    /**
     * TTS 专用 RestTemplate Bean
     * 专门用于 TTS 服务调用，具有更长的超时时间
     */
    @Bean("ttsRestTemplate")
    public RestTemplate ttsRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(30))  // 连接超时：30秒
                .setReadTimeout(Duration.ofMinutes(15))      // 读取超时：15分钟（给TTS更长时间）
                .build();
    }
}