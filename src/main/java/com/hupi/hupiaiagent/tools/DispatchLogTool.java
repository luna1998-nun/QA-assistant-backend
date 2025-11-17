package com.hupi.hupiaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.hupi.hupiaiagent.util.DispatchLogFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 调度日志查询工具（内嵌实现）
 * 使用统一的 DispatchLogFileUtil 解析文件路径
 */
@Component
@Slf4j
public class DispatchLogTool {

    @Autowired
    private DispatchLogFileUtil dispatchLogFileUtil;

    @Tool(description = "查询指定日期的调度日志原文。参数格式：YYYY-MM-DD。存在返回全文，不存在返回空值。")
    public String dispatch_log(@ToolParam(description = "日志日期，格式 YYYY-MM-DD") String date) {
        log.info("=== DispatchLogTool 调用 ===");
        log.info("请求查询日期: {}", date);
        
        if (date == null || date.trim().isEmpty()) {
            log.warn("日期参数为空");
            return null;
        }
        
        String trimmedDate = date.trim();
        File file = dispatchLogFileUtil.resolveLogFile(trimmedDate);
        log.info("读取文件路径: {}", file.getAbsolutePath());
        
        try {
            if (!file.exists() || !file.isFile()) {
                log.warn("文件不存在: {}", file.getAbsolutePath());
                return null;
            }
            
            String content = FileUtil.readUtf8String(file);
            int contentLength = content != null ? content.length() : 0;
            log.info("成功读取文件，内容长度: {} 字符", contentLength);
            if (content != null && content.length() > 0) {
                String preview = content.length() > 200 ? content.substring(0, 200) + "..." : content;
                log.debug("文件内容预览: {}", preview);
            }
            return content;
        } catch (Exception e) {
            log.error("读取文件时发生错误: {}", e.getMessage(), e);
            return null;
        }
    }
}


