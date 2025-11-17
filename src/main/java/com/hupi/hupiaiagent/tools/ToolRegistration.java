package com.hupi.hupiaiagent.tools;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 集中的工具注册类
 */
@Configuration
public class ToolRegistration {

//    @Value("${search-api.api-key}")
//    private String searchApiKey;

    @Bean
    public ToolCallback[] allTools() {
        // 精简为生产调度分析助手所需工具
        TerminateTool terminateTool = new TerminateTool();
        DispatchLogTool dispatchLogTool = new DispatchLogTool();
        return ToolCallbacks.from(
                terminateTool,
                dispatchLogTool
        );
    }
}
