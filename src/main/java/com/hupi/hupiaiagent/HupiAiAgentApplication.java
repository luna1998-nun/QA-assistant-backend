package com.hupi.hupiaiagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.charset.StandardCharsets;

@SpringBootApplication(exclude = {
        com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeChatAutoConfiguration.class,
        com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeEmbeddingAutoConfiguration.class,
        com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeAgentAutoConfiguration.class,
        // 启用TTS功能，移除DashScopeAudioSpeechAutoConfiguration排除
        // com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeAudioSpeechAutoConfiguration.class,
        com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeAudioTranscriptionAutoConfiguration.class,
        com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeImageAutoConfiguration.class,
        com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeRerankAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
})
public class HupiAiAgentApplication {

    public static void main(String[] args) {
        // 设置系统属性，确保控制台输出使用 UTF-8 编码
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");
        System.setProperty("user.language", "zh");
        System.setProperty("user.country", "CN");
        
        // 设置标准输出和错误输出的编码为 UTF-8
        System.setOut(new java.io.PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new java.io.PrintStream(System.err, true, StandardCharsets.UTF_8));
        
        SpringApplication.run(HupiAiAgentApplication.class, args);
    }

}
