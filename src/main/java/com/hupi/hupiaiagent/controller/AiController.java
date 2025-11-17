package com.hupi.hupiaiagent.controller;

import com.hupi.hupiaiagent.agent.HupiManus;
import com.hupi.hupiaiagent.app.DispatchAssistantApp;
import com.hupi.hupiaiagent.util.DispatchLogFileUtil;
import com.hupi.hupiaiagent.util.RealtimeStreamFormatter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/ai")
@Slf4j
public class AiController {

    @Resource
    private DispatchAssistantApp dispatchAssistantApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    @Resource
    private DispatchLogFileUtil dispatchLogFileUtil;

    /**
     * 同步调用 生产调度分析助手
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/dispatch_app/chat/sync")
    public String doChatWithDispatchAppSyncGet(String message, String chatId) {
        return dispatchAssistantApp.doChat(message, chatId);
    }
    
    /**
     * 同步调用 生产调度分析助手（POST 方法，支持长消息）
     */
    @PostMapping("/dispatch_app/chat/sync")
    public String doChatWithDispatchAppSync(@RequestParam(required = false) String message, @RequestParam(required = false) String chatId) {
        return dispatchAssistantApp.doChat(message, chatId);
    }

    /**
     * SSE 流式调用 生产调度分析助手（基础流）
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/dispatch_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithDispatchAppSSE(String message, String chatId) {
        return dispatchAssistantApp.doChatByStream(message, chatId)
                .map(chunk -> "data: " + chunk + "\n\n");
    }

    /**
     * SSE 流式调用 生产调度分析助手（标准 ServerSentEvent）
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/dispatch_app/chat/server_sent_event", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> doChatWithDispatchAppServerSentEvent(String message, String chatId) {
        return dispatchAssistantApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .event("message")
                        .build());
    }

    /**
     * 调试接口：完整的测试和调试信息
     * 返回详细的文件读取、消息构建、模型调用等所有步骤的信息
     */
    @GetMapping("/dispatch_app/debug/prefetch")
    public String debugPrefetch(String date, String chatId) {
        StringBuilder debugInfo = new StringBuilder();
        debugInfo.append("=== 调试信息 ===\n\n");
        
        try {
            if (date == null || date.trim().isEmpty()) {
                debugInfo.append("❌ 错误：date 参数为空\n");
                debugInfo.append("使用方式：GET /api/ai/dispatch_app/debug/prefetch?date=2025-10-20&chatId=test\n");
                return debugInfo.toString();
            }
            
            String normalizedDate = date.trim();
            debugInfo.append("1. 日期参数: ").append(normalizedDate).append("\n\n");
            
            // 清除历史
            if (chatId != null && !chatId.trim().isEmpty()) {
                dispatchAssistantApp.getChatMemory().clear(chatId.trim());
                debugInfo.append("2. 已清除历史对话，chatId: ").append(chatId).append("\n\n");
            } else {
                chatId = "debug-" + normalizedDate;
                debugInfo.append("2. 使用默认 chatId: ").append(chatId).append("\n\n");
            }
            
            // 解析文件路径
            File file = dispatchLogFileUtil.resolveLogFile(normalizedDate);
            debugInfo.append("3. 文件解析结果:\n");
            debugInfo.append("   - 绝对路径: ").append(file.getAbsolutePath()).append("\n");
            debugInfo.append("   - 文件存在: ").append(file.exists()).append("\n");
            debugInfo.append("   - 是文件: ").append(file.isFile()).append("\n");
            if (file.exists()) {
                debugInfo.append("   - 文件大小: ").append(file.length()).append(" 字节\n");
            }
            debugInfo.append("\n");
            
            if (!file.exists() || !file.isFile()) {
                debugInfo.append("❌ 文件不存在或不是文件\n");
                debugInfo.append("请检查路径是否正确，或确认文件是否存在\n");
                return debugInfo.toString();
            }
            
            // 读取文件
            String logText = java.nio.file.Files.readString(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            debugInfo.append("4. 文件读取成功:\n");
            debugInfo.append("   - 内容长度: ").append(logText.length()).append(" 字符\n");
            debugInfo.append("   - 行数（估算）: ").append(logText.split("\n").length).append(" 行\n");
            debugInfo.append("   - 前200字符预览:\n");
            debugInfo.append("     ").append(logText.length() > 200 ? logText.substring(0, 200) + "..." : logText).append("\n\n");
            
            // 构建消息（直接发送日志内容，SYSTEM_PROMPT 已定义规则）
            String message = logText;
            
            debugInfo.append("5. 构建的消息:\n");
            debugInfo.append("   - 消息总长度: ").append(message.length()).append(" 字符\n");
            debugInfo.append("   - 消息前300字符:\n");
            debugInfo.append("     ").append(message.length() > 300 ? message.substring(0, 300) + "..." : message).append("\n\n");
            
            // 调用模型
            debugInfo.append("6. 开始调用模型...\n");
            long startTime = System.currentTimeMillis();
            
            try {
                String result = dispatchAssistantApp.doChat(message, chatId);
                long duration = System.currentTimeMillis() - startTime;
                
                debugInfo.append("   调用成功！耗时: ").append(duration).append(" 毫秒\n\n");
                debugInfo.append("7. 模型返回结果:\n");
                debugInfo.append("   - 结果长度: ").append(result != null ? result.length() : 0).append(" 字符\n");
                debugInfo.append("   - 结果内容:\n");
                debugInfo.append("---\n");
                debugInfo.append(result != null ? result : "(空)");
                debugInfo.append("\n---\n\n");
                
                debugInfo.append("✅ 调试完成！\n");
                
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                debugInfo.append("   调用失败！耗时: ").append(duration).append(" 毫秒\n");
                debugInfo.append("   错误类型: ").append(e.getClass().getSimpleName()).append("\n");
                debugInfo.append("   错误信息: ").append(e.getMessage()).append("\n");
                debugInfo.append("   堆栈跟踪:\n");
                for (StackTraceElement ste : e.getStackTrace()) {
                    if (ste.getClassName().contains("hupiaiagent")) {
                        debugInfo.append("     ").append(ste.toString()).append("\n");
                    }
                }
                debugInfo.append("\n");
            }
            
        } catch (Exception e) {
            debugInfo.append("❌ 调试过程发生异常:\n");
            debugInfo.append("   错误类型: ").append(e.getClass().getSimpleName()).append("\n");
            debugInfo.append("   错误信息: ").append(e.getMessage()).append("\n");
            debugInfo.append("   堆栈跟踪:\n");
            for (StackTraceElement ste : e.getStackTrace()) {
                if (ste.getClassName().contains("hupiaiagent")) {
                    debugInfo.append("     ").append(ste.toString()).append("\n");
                }
            }
        }
        
        return debugInfo.toString();
    }

    /**
     * 同步调用（后端预取日志）：先读 tmp/dispatch-logs/{date}.txt，再让模型写
     * 注意：每次调用都会清除该 chatId 的历史，确保只基于本次提供的日志生成
     */
    @GetMapping("/dispatch_app/chat/prefetch/sync")
    public String doChatWithPrefetchSync(String date, String chatId) {
        try {
            if (date == null || date.trim().isEmpty()) {
                return "错误：date 不能为空 (期望 YYYY-MM-DD)";
            }
            String normalizedDate = date.trim();
            
            // 清除历史对话，确保每次都是全新的
            if (chatId != null && !chatId.trim().isEmpty()) {
                dispatchAssistantApp.getChatMemory().clear(chatId.trim());
                log.info("[prefetch-sync] cleared chat history for chatId: {}", chatId);
            }
            
            File file = dispatchLogFileUtil.resolveLogFile(normalizedDate);
            log.info("[prefetch-sync] date={}, resolvedPath={}, exists={}", normalizedDate, file.getAbsolutePath(), file.exists());
            
            if (!file.exists() || !file.isFile()) {
                log.warn("[prefetch-sync] file not found: {}", file.getAbsolutePath());
                return "错误：未查询到 [" + normalizedDate + "] 的调度日志。文件路径: " + file.getAbsolutePath();
            }
            
            String logText = java.nio.file.Files.readString(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            log.info("[prefetch-sync] read ok, length={} chars", logText.length());
            
            // 构建用户消息
            String message = String.format(
                    "请基于[%s]的调度日志生成交接班总结：\n\n%s",
                    normalizedDate, logText
            );
            
            return dispatchAssistantApp.doChat(message, chatId != null ? chatId : "prefetch-" + normalizedDate);
        } catch (Exception e) {
            log.error("prefetch sync error", e);
            return "错误：读取日志失败 - " + e.getMessage();
        }
    }

    /**
     * 流式调用（后端预取日志）：先读日志，再让模型流式生成
     * 注意：每次调用都会清除该 chatId 的历史，确保只基于本次提供的日志生成
     */
    @GetMapping(value = "/dispatch_app/chat/prefetch/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithPrefetchSseGet(String date, String chatId) {
        return doChatWithPrefetchSseInternal(date, chatId);
    }
    
    /**
     * 流式调用（后端预取日志）：先读日志，再让模型流式生成（POST 方法，支持长参数）
     */
    @PostMapping(value = "/dispatch_app/chat/prefetch/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithPrefetchSse(@RequestParam(required = false) String date, @RequestParam(required = false) String chatId) {
        return doChatWithPrefetchSseInternal(date, chatId);
    }
    
    /**
     * 内部实现方法（共享逻辑）
     */
    private Flux<String> doChatWithPrefetchSseInternal(String date, String chatId) {
        try {
            if (date == null || date.trim().isEmpty()) {
                return Flux.just("data: 错误：date 不能为空 (期望 YYYY-MM-DD)\n\n");
            }
            String normalizedDate = date.trim();
            
            // 清除历史对话，确保每次都是全新的
            if (chatId != null && !chatId.trim().isEmpty()) {
                dispatchAssistantApp.getChatMemory().clear(chatId.trim());
                log.info("[prefetch-sse] cleared chat history for chatId: {}", chatId);
            }
            
            File file = dispatchLogFileUtil.resolveLogFile(normalizedDate);
            log.info("[prefetch-sse] date={}, resolvedPath={}, exists={}", normalizedDate, file.getAbsolutePath(), file.exists());
            
            if (!file.exists() || !file.isFile()) {
                log.warn("[prefetch-sse] file not found: {}", file.getAbsolutePath());
                return Flux.just("data: 错误：未查询到 [" + normalizedDate + "] 的调度日志。文件路径: " + file.getAbsolutePath() + "\n\n");
            }
            
            String logText = java.nio.file.Files.readString(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            log.info("[prefetch-sse] read ok, length={} chars", logText.length());
            
            // 构建用户消息
            String message = String.format(
                    "请基于[%s]的调度日志生成交接班总结：\n\n%s",
                    normalizedDate, logText
            );
            
            return dispatchAssistantApp.doChatByStream(message, chatId != null ? chatId : "prefetch-" + normalizedDate)
                    .map(chunk -> "data: " + chunk + "\n\n");
        } catch (Exception e) {
            log.error("prefetch sse error", e);
            return Flux.just("data: 错误：读取日志失败 - " + e.getMessage() + "\n\n");
        }
    }

    /**
     * SSE 流式调用 生产调度分析助手（增强：thinking/message 分发）
     * GET 方法（保持向后兼容）
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/dispatch_app/chat/sse_emitter", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public SseEmitter doChatWithDispatchAppServerSseEmitterGet(@RequestParam(required = false) String message, @RequestParam(required = false) String chatId) {
        return doChatWithDispatchAppServerSseEmitterInternal(message, chatId);
    }
    
    /**
     * SSE 流式调用 生产调度分析助手（POST 方法，支持长消息）
     *
     * @param message
     * @param chatId
     * @return
     */
    @PostMapping(value = "/dispatch_app/chat/sse_emitter", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public SseEmitter doChatWithDispatchAppServerSseEmitter(@RequestParam(required = false) String message, @RequestParam(required = false) String chatId) {
        return doChatWithDispatchAppServerSseEmitterInternal(message, chatId);
    }
    
    /**
     * 内部实现方法（共享逻辑）
     */
    private SseEmitter doChatWithDispatchAppServerSseEmitterInternal(String message, String chatId) {
        // 创建一个超时时间较长的 SseEmitter（10分钟，处理大量日志内容）
        SseEmitter sseEmitter = new SseEmitter(600000L); // 10 分钟超时
        
        log.info("[SSE] Starting chat stream, message length: {}, chatId: {}", 
                message != null ? message.length() : 0, chatId);
        
        // 设置响应头，确保使用 UTF-8 编码
        try {
            org.springframework.web.context.request.RequestAttributes requestAttributes = 
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof org.springframework.web.context.request.ServletRequestAttributes) {
                jakarta.servlet.http.HttpServletResponse response = 
                    ((org.springframework.web.context.request.ServletRequestAttributes) requestAttributes)
                        .getResponse();
                if (response != null) {
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("text/event-stream;charset=UTF-8");
                }
            }
        } catch (Exception e) {
            log.warn("[SSE] Failed to set response encoding, may cause encoding issues", e);
        }
        
        // 参数验证
        if (message == null || message.trim().isEmpty()) {
            log.warn("[SSE] message parameter is null or empty");
            try {
                sseEmitter.send(SseEmitter.event()
                        .name("error")
                        .data("错误：message 参数不能为空"));
                sseEmitter.complete();
                return sseEmitter;
            } catch (IOException e) {
                log.error("[SSE] Error sending parameter validation error", e);
                sseEmitter.completeWithError(e);
                return sseEmitter;
            }
        }
        
        // 如果 chatId 为空，使用默认值
        if (chatId == null || chatId.trim().isEmpty()) {
            chatId = "default-" + System.currentTimeMillis();
            log.info("[SSE] Using default chatId: {}", chatId);
        }
        
        // 设置字符编码
        sseEmitter.onCompletion(() -> log.info("[SSE] Connection completed"));
        sseEmitter.onTimeout(() -> {
            log.warn("[SSE] Connection timeout after 10 minutes");
            try {
                sseEmitter.send(SseEmitter.event()
                        .name("error")
                        .data("请求超时，请重试"));
                sseEmitter.complete();
            } catch (IOException e) {
                log.error("[SSE] Error sending timeout message", e);
            }
        });
        sseEmitter.onError(throwable -> {
            log.error("[SSE] Connection error", throwable);
            try {
                sseEmitter.send(SseEmitter.event()
                        .name("error")
                        .data("连接错误: " + throwable.getMessage()));
                sseEmitter.complete();
            } catch (IOException e) {
                log.error("[SSE] Error sending error message", e);
            }
        });
        
        // 用于追踪内容和状态
        final StringBuilder fullContent = new StringBuilder();
        final AtomicBoolean inThinkingBlock = new AtomicBoolean(false);
        final RealtimeStreamFormatter formatter = new RealtimeStreamFormatter();
        
        try {
            // 获取 Flux 响应式数据流并且直接通过订阅推送给 SseEmitter
            dispatchAssistantApp.doChatByStream(message, chatId)
                    .doOnSubscribe(subscription -> {
                        log.info("[SSE] Stream subscription started");
                    })
                    .doOnError(error -> {
                        log.error("[SSE] Stream error occurred", error);
                    })
                    .doOnComplete(() -> {
                        log.info("[SSE] Stream completed");
                    })
                    .subscribe(chunk -> {
                        try {
                            if (chunk == null || chunk.isEmpty()) {
                                return;
                            }
                            
                            fullContent.append(chunk);
                            String accumulated = fullContent.toString();
                            
                            // 查找thinking标签
                            boolean hasThinkStart = accumulated.contains("<think>");
                            boolean hasThinkEnd = accumulated.contains("</think>");
                            
                            // 清理标签字符
                            String cleanChunk = chunk.replace("<think>", "")
                                                   .replace("</think>", "");
                            
                            // 判断当前内容类型
                            boolean isThinkingContent = hasThinkStart && !hasThinkEnd;
                            
                            // 更新状态
                            if (hasThinkStart && !inThinkingBlock.get()) {
                                inThinkingBlock.set(true);
                            }
                            if (hasThinkEnd && inThinkingBlock.get()) {
                                inThinkingBlock.set(false);
                            }
                            
                            // 只发送非空内容
                            if (!cleanChunk.trim().isEmpty()) {
                                if (isThinkingContent || inThinkingBlock.get()) {
                                    // 发送thinking事件（不格式化思考内容）
                                    sseEmitter.send(SseEmitter.event()
                                            .name("thinking")
                                            .data(cleanChunk));
                                } else {
                                    // 使用实时格式化器处理chunk，逐字实时格式化
                                    String formattedChunk = formatter.processChunk(cleanChunk);
                                    
                                    // 立即发送格式化后的chunk（保持流式输出）
                                    if (!formattedChunk.isEmpty()) {
                                        sseEmitter.send(SseEmitter.event()
                                                .name("message")
                                                .data(formattedChunk));
                                    }
                                }
                            }
                        } catch (IOException e) {
                            log.error("[SSE] Error sending SSE data chunk", e);
                            try {
                                sseEmitter.send(SseEmitter.event()
                                        .name("error")
                                        .data("发送数据错误: " + e.getMessage()));
                                sseEmitter.completeWithError(e);
                            } catch (IOException ex) {
                                log.error("[SSE] Error completing after send error", ex);
                            }
                        } catch (Exception e) {
                            log.error("[SSE] Unexpected error processing chunk", e);
                            try {
                                sseEmitter.send(SseEmitter.event()
                                        .name("error")
                                        .data("处理数据错误: " + e.getMessage()));
                                sseEmitter.completeWithError(e);
                            } catch (IOException ex) {
                                log.error("[SSE] Error completing after processing error", ex);
                            }
                        }
                    }, error -> {
                        log.error("[SSE] Error in stream processing", error);
                        try {
                            sseEmitter.send(SseEmitter.event()
                                    .name("error")
                                    .data("流处理错误: " + (error.getMessage() != null ? error.getMessage() : error.getClass().getSimpleName())));
                            sseEmitter.complete();
                        } catch (IOException e) {
                            log.error("[SSE] Error completing after stream error", e);
                            sseEmitter.completeWithError(error);
                        }
                    }, () -> {
                        try {
                            log.info("[SSE] Stream finished, total content length: {}", fullContent.length());
                            // 处理剩余的缓冲区内容
                            String remaining = formatter.finish();
                            if (!remaining.isEmpty()) {
                                sseEmitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(remaining));
                            }
                            
                            sseEmitter.send(SseEmitter.event()
                                    .name("complete")
                                    .data("Stream completed"));
                            sseEmitter.complete();
                        } catch (IOException e) {
                            log.error("[SSE] Error completing SSE", e);
                            sseEmitter.completeWithError(e);
                        }
                    });
        } catch (Exception e) {
            log.error("[SSE] Error setting up stream", e);
            try {
                sseEmitter.send(SseEmitter.event()
                        .name("error")
                        .data("设置流失败: " + e.getMessage()));
                sseEmitter.complete();
            } catch (IOException ex) {
                log.error("[SSE] Error completing after setup error", ex);
                sseEmitter.completeWithError(e);
            }
        }
        
        // 返回
        return sseEmitter;
    }

    /**
     * SSE 流式调用（启用工具）的 生产调度分析助手
     */
    @GetMapping(value = "/dispatch_app/chat/sse_emitter_tools", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public SseEmitter doChatWithToolsSseEmitter(String message, String chatId) {
        // 创建一个超时时间较长的 SseEmitter（10分钟，处理大量日志内容）
        SseEmitter sseEmitter = new SseEmitter(600000L); // 10 分钟超时
        
        log.info("[SSE-Tools] Starting chat stream with tools, message length: {}, chatId: {}", 
                message != null ? message.length() : 0, chatId);
        
        // 设置响应头，确保使用 UTF-8 编码
        try {
            org.springframework.web.context.request.RequestAttributes requestAttributes = 
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof org.springframework.web.context.request.ServletRequestAttributes) {
                jakarta.servlet.http.HttpServletResponse response = 
                    ((org.springframework.web.context.request.ServletRequestAttributes) requestAttributes)
                        .getResponse();
                if (response != null) {
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("text/event-stream;charset=UTF-8");
                }
            }
        } catch (Exception e) {
            log.warn("[SSE-Tools] Failed to set response encoding, may cause encoding issues", e);
        }
        
        sseEmitter.onCompletion(() -> log.info("[SSE-Tools] Connection completed"));
        sseEmitter.onTimeout(() -> {
            log.warn("[SSE-Tools] Connection timeout after 10 minutes");
            try {
                sseEmitter.send(SseEmitter.event()
                        .name("error")
                        .data("请求超时，请重试"));
                sseEmitter.complete();
            } catch (IOException e) {
                log.error("[SSE-Tools] Error sending timeout message", e);
            }
        });
        sseEmitter.onError(throwable -> {
            log.error("[SSE-Tools] Connection error", throwable);
            try {
                sseEmitter.send(SseEmitter.event()
                        .name("error")
                        .data("连接错误: " + throwable.getMessage()));
                sseEmitter.complete();
            } catch (IOException e) {
                log.error("[SSE-Tools] Error sending error message", e);
            }
        });

        final StringBuilder fullContent = new StringBuilder();
        final AtomicBoolean inThinkingBlock = new AtomicBoolean(false);

        try {
            dispatchAssistantApp.doChatWithToolsByStream(message, chatId)
                    .doOnSubscribe(subscription -> {
                        log.info("[SSE-Tools] Stream subscription started");
                    })
                    .doOnError(error -> {
                        log.error("[SSE-Tools] Stream error occurred", error);
                    })
                    .doOnComplete(() -> {
                        log.info("[SSE-Tools] Stream completed");
                    })
                    .subscribe(chunk -> {
                        try {
                            if (chunk == null || chunk.isEmpty()) {
                                return;
                            }
                            
                            fullContent.append(chunk);
                            String accumulated = fullContent.toString();

                            boolean hasThinkStart = accumulated.contains("<think>");
                            boolean hasThinkEnd = accumulated.contains("</think>");

                            String cleanChunk = chunk.replace("<think>", "")
                                    .replace("</think>", "")
                                    .replace("</think>", "")
                                    .replace("<think>", "");

                            boolean isThinkingContent = hasThinkStart && !hasThinkEnd;

                            if (hasThinkStart && !inThinkingBlock.get()) {
                                inThinkingBlock.set(true);
                            }
                            if (hasThinkEnd && inThinkingBlock.get()) {
                                inThinkingBlock.set(false);
                            }

                            if (!cleanChunk.trim().isEmpty()) {
                                if (isThinkingContent || inThinkingBlock.get()) {
                                    sseEmitter.send(SseEmitter.event().name("thinking").data(cleanChunk));
                                } else {
                                    sseEmitter.send(SseEmitter.event().name("message").data(cleanChunk));
                                }
                            }
                        } catch (IOException e) {
                            log.error("[SSE-Tools] Error sending SSE data chunk", e);
                            try {
                                sseEmitter.send(SseEmitter.event()
                                        .name("error")
                                        .data("发送数据错误: " + e.getMessage()));
                                sseEmitter.completeWithError(e);
                            } catch (IOException ex) {
                                log.error("[SSE-Tools] Error completing after send error", ex);
                            }
                        } catch (Exception e) {
                            log.error("[SSE-Tools] Unexpected error processing chunk", e);
                            try {
                                sseEmitter.send(SseEmitter.event()
                                        .name("error")
                                        .data("处理数据错误: " + e.getMessage()));
                                sseEmitter.completeWithError(e);
                            } catch (IOException ex) {
                                log.error("[SSE-Tools] Error completing after processing error", ex);
                            }
                        }
                    }, error -> {
                        log.error("[SSE-Tools] Error in stream processing", error);
                        try {
                            sseEmitter.send(SseEmitter.event()
                                    .name("error")
                                    .data("流处理错误: " + (error.getMessage() != null ? error.getMessage() : error.getClass().getSimpleName())));
                            sseEmitter.complete();
                        } catch (IOException e) {
                            log.error("[SSE-Tools] Error completing after stream error", e);
                            sseEmitter.completeWithError(error);
                        }
                    }, () -> {
                        try {
                            log.info("[SSE-Tools] Stream finished, total content length: {}", fullContent.length());
                            sseEmitter.send(SseEmitter.event().name("complete").data("Stream completed"));
                            sseEmitter.complete();
                        } catch (IOException e) {
                            log.error("[SSE-Tools] Error completing SSE", e);
                            sseEmitter.completeWithError(e);
                        }
                    });
        } catch (Exception e) {
            log.error("[SSE-Tools] Error setting up stream", e);
            try {
                sseEmitter.send(SseEmitter.event()
                        .name("error")
                        .data("设置流失败: " + e.getMessage()));
                sseEmitter.complete();
            } catch (IOException ex) {
                log.error("[SSE-Tools] Error completing after setup error", ex);
                sseEmitter.completeWithError(e);
            }
        }

        return sseEmitter;
    }

    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message
     * @return
     */
    @GetMapping(value = "/manus/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter doChatWithManus(String message) {
        HupiManus hupiManus = new HupiManus(allTools, dashscopeChatModel);
        return hupiManus.runStream(message);
    }

    /**
     * 获取所有对话历史列表（生产调度分析助手）
     */
    @GetMapping("/dispatch_app/chat/history/list")
    public List<ChatHistoryItem> getChatHistoryList() {
        try {
            ChatMemory chatMemory = dispatchAssistantApp.getChatMemory();
            
            // 获取文件目录
            String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
            File directory = new File(fileDir);
            
            if (!directory.exists() || !directory.isDirectory()) {
                return new ArrayList<>();
            }
            
            // 读取所有 .kryo 文件
            File[] files = directory.listFiles((dir, name) -> name.endsWith(".kryo"));
            
            if (files == null || files.length == 0) {
                return new ArrayList<>();
            }
            
            // 转换为 ChatHistoryItem
            List<ChatHistoryItem> historyList = new ArrayList<>();
            
            for (File file : files) {
                try {
                    String chatId = file.getName().replace(".kryo", "");
                    List<Message> messages = chatMemory.get(chatId);
                    
                    if (messages != null && !messages.isEmpty()) {
                        // 获取第一条用户消息作为标题
                        String title = extractTitle(messages);
                        String time = formatFileTime(file.lastModified());
                        
                        historyList.add(new ChatHistoryItem(chatId, title, time, messages.size()));
                    }
                } catch (Exception e) {
                    log.error("Error reading chat file: " + file.getName(), e);
                }
            }
            
            // 按时间倒序排序
            historyList.sort((a, b) -> b.time.compareTo(a.time));
            
            return historyList;
        } catch (Exception e) {
            log.error("Error getting chat history list", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取单个对话的详细内容（生产调度分析助手）
     */
    @GetMapping("/dispatch_app/chat/history/detail")
    public List<ChatMessageDto> getChatHistoryDetail(String chatId) {
        try {
            ChatMemory chatMemory = dispatchAssistantApp.getChatMemory();
            List<Message> messages = chatMemory.get(chatId);
            
            if (messages == null || messages.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 转换为 DTO
            List<ChatMessageDto> dtos = new ArrayList<>();
            for (Message message : messages) {
                String content = extractMessageContent(message);
                String role = "assistant";
                if (message.getClass().getSimpleName().contains("User")) {
                    role = "user";
                } else if (message.getClass().getSimpleName().contains("System")) {
                    role = "system";
                }
                dtos.add(new ChatMessageDto(role, content));
            }
            
            return dtos;
        } catch (Exception e) {
            log.error("Error getting chat history detail for chatId: " + chatId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 从 Message 对象中提取内容
     */
    private String extractMessageContent(Message message) {
        try {
            // 尝试 getContent
            var getContent = message.getClass().getMethod("getContent");
            String content = (String) getContent.invoke(message);
            if (content != null && !content.isEmpty()) {
                return content;
            }
        } catch (Exception e) {
            log.debug("getContent() not found");
        }
        
        try {
            // 尝试 getText
            var getText = message.getClass().getMethod("getText");
            String text = (String) getText.invoke(message);
            if (text != null && !text.isEmpty()) {
                return text;
            }
        } catch (Exception e) {
            log.debug("getText() not found");
        }
        
        return "";
    }

    /**
     * 删除指定对话（生产调度分析助手）
     */
    @DeleteMapping("/dispatch_app/chat/history/delete")
    public void deleteChatHistory(String chatId) {
        try {
            ChatMemory chatMemory = dispatchAssistantApp.getChatMemory();
            chatMemory.clear(chatId);
            log.info("Deleted chat history for chatId: " + chatId);
        } catch (Exception e) {
            log.error("Error deleting chat history for chatId: " + chatId, e);
            throw new RuntimeException("Failed to delete chat history", e);
        }
    }

    /**
     * 从消息列表中提取标题（第一条用户消息的前50个字符）
     */
    private String extractTitle(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return "新对话";
        }
        
        try {
            for (Message message : messages) {
                // 检查是否为用户消息
                if (message.getClass().getSimpleName().contains("User")) {
                    // 使用反射获取内容
                    try {
                        var getContent = message.getClass().getMethod("getContent");
                        String content = (String) getContent.invoke(message);
                        if (content != null && !content.isEmpty()) {
                            // 取前50个字符作为标题
                            String title = content.length() > 50 ? content.substring(0, 50) + "..." : content;
                            return title.replace("\n", " ").trim();
                        }
                    } catch (Exception e) {
                        // 如果 getContent 不存在，尝试 getText
                        try {
                            var getText = message.getClass().getMethod("getText");
                            String text = (String) getText.invoke(message);
                            if (text != null && !text.isEmpty()) {
                                String title = text.length() > 50 ? text.substring(0, 50) + "..." : text;
                                return title.replace("\n", " ").trim();
                            }
                        } catch (Exception e2) {
                            log.debug("Could not extract text from message", e2);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error extracting title from messages", e);
        }
        
        return "新对话";
    }

    /**
     * 格式化文件时间
     */
    private String formatFileTime(long millis) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }

    /**
     * 对话历史项
     */
    public record ChatHistoryItem(String id, String title, String time, int messageCount) {}
    
    /**
     * 对话消息 DTO
     */
    public record ChatMessageDto(String role, String content) {}
}
