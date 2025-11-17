package com.hupi.hupiaiagent.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 统一调度日志文件路径解析工具
 */
@Component
@Slf4j
public class DispatchLogFileUtil {

    @Value("${dispatch.logs.dir:}")
    private String dispatchLogsDir;

    /**
     * 解析日志文件路径，支持多种路径查找方式
     * @param date 日期 YYYY-MM-DD
     * @return 日志文件，如果不存在则返回一个占位文件（用于日志输出）
     */
    public File resolveLogFile(String date) {
        // 优先使用外部配置的目录
        if (dispatchLogsDir != null && !dispatchLogsDir.trim().isEmpty()) {
            File f = new File(dispatchLogsDir.trim(), date + ".txt");
            if (f.exists() && f.isFile()) {
                log.info("[DispatchLogFileUtil] resolve by configured dir: {}", f.getAbsolutePath());
                return f;
            } else {
                log.warn("[DispatchLogFileUtil] not found in configured dir: {}", f.getAbsolutePath());
            }
        }
        
        // 兼容不同启动目录的两种常见写法
        String userDir = System.getProperty("user.dir");
        File f1 = new File(userDir + "/tmp/dispatch-logs/" + date + ".txt");
        if (f1.exists() && f1.isFile()) {
            log.info("[DispatchLogFileUtil] resolve by user.dir (path1): {}", f1.getAbsolutePath());
            return f1;
        }
        
        File f2 = new File(new File(userDir, "tmp/dispatch-logs"), date + ".txt");
        if (f2.exists() && f2.isFile()) {
            log.info("[DispatchLogFileUtil] resolve by user.dir (path2): {}", f2.getAbsolutePath());
            return f2;
        }
        
        // 返回一个占位文件用于统一日志输出
        File placeholder = new File(userDir, "tmp/dispatch-logs/" + date + ".txt");
        log.warn("[DispatchLogFileUtil] file not found, placeholder: {}", placeholder.getAbsolutePath());
        return placeholder;
    }
}

