package com.hupi.hupiaiagent.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Ollama HTTP 客户端超时配置
 * 设置连接超时和读取超时，避免长时间等待
 */
@Configuration
@Slf4j
public class OllamaHttpClientConfig {

    /**
     * 配置 Apache HttpClient 5 用于 Ollama 请求
     * 设置较长的超时时间和连接池，处理大量日志内容
     */
    @Bean(name = "ollamaHttpClient")
    public HttpClient ollamaHttpClient() {
        // 连接池配置
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100); // 最大连接数
        connectionManager.setDefaultMaxPerRoute(20); // 每个路由的最大连接数
        
        // Socket 配置（连接超时）
        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(Timeout.ofMinutes(10)) // Socket 读取超时：10分钟
                .build();
        connectionManager.setDefaultSocketConfig(socketConfig);
        
        // 连接配置 - 增加连接超时时间到60秒
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(60)) // 连接超时：60秒（增加连接超时时间）
                .setSocketTimeout(Timeout.ofMinutes(10)) // Socket 超时：10分钟
                .build();
        connectionManager.setDefaultConnectionConfig(connectionConfig);
        
        // 请求配置
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(30)) // 从连接池获取连接的超时时间
                .setResponseTimeout(Timeout.ofMinutes(10)) // 响应超时：10分钟
                .build();
        
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .evictIdleConnections(Timeout.ofMinutes(5)) // 空闲连接清理
                .evictExpiredConnections() // 清理过期连接
                .build();
        
        log.info("Ollama HttpClient configured with: connectTimeout=60s, readTimeout=10min, maxTotal=100");
        return httpClient;
    }

    /**
     * 配置 RestTemplate 用于 Ollama 请求（如果需要）
     * 使用上面配置的 HttpClient
     */
    @Bean(name = "ollamaRestTemplate")
    public RestTemplate ollamaRestTemplate(RestTemplateBuilder builder) {
        HttpClient httpClient = ollamaHttpClient();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient);
        
        log.info("Ollama RestTemplate configured with custom HttpClient");
        return builder
                .requestFactory(() -> factory)
                .build();
    }

    /**
     * 配置 RestClient.Builder 用于 Spring AI Ollama
     * Spring AI 1.0 使用 RestClient，我们需要配置自定义的 HTTP 客户端
     */
    @Bean(name = "ollamaRestClientBuilder")
    public RestClient.Builder ollamaRestClientBuilder() {
        HttpClient httpClient = ollamaHttpClient();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient);
        
        log.info("Ollama RestClient.Builder configured with custom HttpClient");
        return RestClient.builder()
                .requestFactory(factory);
    }

    /**
     * 测试 Ollama 服务器连接
     * 在应用启动时测试连接，帮助诊断问题
     */
    @Bean
    public OllamaConnectionTest ollamaConnectionTest() {
        return new OllamaConnectionTest();
    }

    /**
     * Ollama 连接测试类
     */
    @Slf4j
    public static class OllamaConnectionTest {
        
        public OllamaConnectionTest() {
            // 延迟测试，等待 Spring 上下文完全初始化
            new Thread(() -> {
                try {
                    Thread.sleep(5000); // 等待5秒
                    testConnection();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
        
        private void testConnection() {
            String ollamaUrl = "http://10.78.183.178:11434/api/tags";
            log.info("Testing Ollama connection to: {}", ollamaUrl);
            
            try {
                java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(ollamaUrl))
                        .GET()
                        .timeout(Duration.ofSeconds(10))
                        .build();
                
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    log.info("✅ Ollama connection test successful! Status: {}", response.statusCode());
                } else {
                    log.warn("⚠️ Ollama connection test returned status: {}", response.statusCode());
                }
            } catch (Exception e) {
                log.error("❌ Ollama connection test failed: {}", e.getMessage());
                log.error("Please check:");
                log.error("1. Ollama server is running at http://10.78.183.178:11434");
                log.error("2. Network connectivity (ping 10.78.183.178)");
                log.error("3. Firewall settings");
                log.error("4. Port 11434 is accessible");
            }
        }
    }
}

