package com.hupi.hupiaiagent.app;

import com.hupi.hupiaiagent.advisor.MyLoggerAdvisor;
import com.hupi.hupiaiagent.chatmemory.FileBasedChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@Slf4j
public class DispatchAssistantApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
            你是油气田生产调度总结助手。根据用户请求生成"YYYY-MM-DD交接班总结"。

            任务：
            1) 解析日期：从用户输入（如"昨天/10月19日"）解析为 YYYY-MM-DD；每次都以本次解析的日期为准。
            2) 昨日处理主要工作：
               - 仅保留与 清管/检修/中断或停输/抢修/隐患治理/技改/产量影响（增产、降产、欠产、停输）/配合/协调/申请/汇报（非纯数字上报） 相关的条目；
               - 严格忽略以下类型的日志条目：
                 * 所有包含"完成商品量"、"实际完成商品量"、"公司调度商品量"、"计划商品量"、"欠产"、"超产"等纯计产类上报的日志；
                 * 所有仅包含数字产量数据、无实质性调度工作内容的日志；
                 * 视频监控、巡检、例行检查等与生产调度决策无关的日常操作日志；
               - 每条格式要求：
                 * 时间必须放在句子最开头，格式为 HH:mm（如 18:24、16:37），从日志行首时间戳提取，不要包含日期部分；
                 * 时间后紧跟单位、设备或线路、事件、状态/计划/影响等内容，用自然语言完整句子表述；
                 * 不得使用"时间：/单位：/设备："等字段标签拼接；
                 * 正确格式示例：18:24第二输油处白豹作业区铁西线0-13光缆中断，预计22:00恢复；
                 * 错误格式示例：2025/10/1918:24第二输油处...（时间不应包含日期，且时间应在开头）
               - 严禁编造或泛化实体名称（如"X油田""某生产线"），必须原样引用日志中的真实单位/设备/线路名称。
            3) 今日关注工作：从日志中识别未来计划，以及昨日未完（正在抢修/预计X小时/状态未明确完成）的事项，并逐条用自然句式概括（同样包含单位、设备/线路、工作内容/状态，不得使用字段标签，不得编造）。
            4) 输出格式：
               - 按"YYYY-MM-DD交接班总结"标题开头
               - 然后是"昨日处理主要工作"小节
               - 最后是"今日关注工作"小节
               - 每个小节下，条目按照时间从早到晚排序，条目前以"1."、"2."等编号
            """;

    private final ChatMemory chatMemory;

    public DispatchAssistantApp(ChatModel dashscopeChatModel) {
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        chatMemory = new FileBasedChatMemory(fileDir);

        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new MyLoggerAdvisor()
                )
                .build();
    }

    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = (chatResponse != null && chatResponse.getResult() != null
                && chatResponse.getResult().getOutput() != null)
                ? chatResponse.getResult().getOutput().getText()
                : "";
        log.info("content: {}", content);
        // 格式化输出内容，按语义进行换行
        return formatContent(content);
    }

    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

    @Resource
    private ToolCallback[] allTools;

    public String doChatWithTools(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = (chatResponse != null && chatResponse.getResult() != null
                && chatResponse.getResult().getOutput() != null)
                ? chatResponse.getResult().getOutput().getText()
                : "";
        log.info("content: {}", content);
        // 格式化输出内容，按语义进行换行
        return formatContent(content);
    }

    public Flux<String> doChatWithToolsByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(allTools)
                .stream()
                .content();
    }

    public ChatMemory getChatMemory() {
        return chatMemory;
    }

    /**
     * 格式化输出内容，按语义进行换行
     * 识别序号、小节标题、日期标题等，在适当位置添加换行
     * 同时过滤掉提示词内容
     */
    private String formatContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }

        // 首先过滤掉提示词内容（输出说明等）
        String cleaned = content;
        // 移除"输出说明"及其后面的所有内容
        int outputNoteIndex = cleaned.indexOf("输出说明");
        if (outputNoteIndex >= 0) {
            cleaned = cleaned.substring(0, outputNoteIndex);
        }
        // 移除其他可能的提示词标记
        cleaned = cleaned.replaceAll("(?i)(格式要求|注意事项|说明：|提示：).*", "");
        
        // 使用正则表达式在关键位置添加换行
        // 注意：序号可能没有空格，如"1."而不是"1. "
        String formatted = cleaned
            // 在"交接班总结"后添加换行（如果后面不是换行）
            .replaceAll("(交接班总结)([^\\n])", "$1\n$2")
            // 在小节标题前添加换行（如果前面不是换行）
            .replaceAll("([^\\n])(昨日处理主要工作|今日关注工作)", "$1\n$2")
            // 在日期标题前添加两个换行
            .replaceAll("([^\\n])(\\d{4}-\\d{2}-\\d{2})", "$1\n\n$2")
            // 处理小节标题后直接跟序号的情况（如"昨日处理主要工作1."）
            .replaceAll("(昨日处理主要工作|今日关注工作)(\\d+[.、])", "$1\n$2")
            // 在序号前添加换行（数字序号：1. 2. 3. 等，可能没有空格）
            // 匹配：非数字非换行字符 + 数字序号
            .replaceAll("([^\\n\\d])(\\d+[.、])", "$1\n$2")
            // 处理连续序号的情况（如"1.2."应该分开）
            .replaceAll("(\\d+[.、])(\\d+[.、])", "$1\n$2")
            // 在中文序号前添加换行（一、二、三等）
            .replaceAll("([^\\n])([一二三四五六七八九十]+[、.])", "$1\n$2");

        // 按行处理，确保格式正确
        StringBuilder result = new StringBuilder();
        String[] lines = formatted.split("\n", -1);
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();
            
            if (trimmed.isEmpty()) {
                // 保留单个空行
                if (result.length() > 0 && !result.toString().endsWith("\n\n")) {
                    result.append("\n");
                }
                continue;
            }
            
            // 日期标题：后跟两个换行
            if (trimmed.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
                if (result.length() > 0 && !result.toString().endsWith("\n\n")) {
                    result.append("\n");
                }
                result.append(trimmed).append("\n\n");
                continue;
            }
            
            // "交接班总结"标题：后跟一个换行
            if (trimmed.contains("交接班总结") && trimmed.length() < 20) {
                result.append(trimmed).append("\n");
                continue;
            }
            
            // 小节标题：后跟一个换行
            if (trimmed.contains("昨日处理主要工作") || trimmed.contains("今日关注工作") ||
                (trimmed.length() < 15 && (trimmed.contains("主要工作") || trimmed.contains("关注工作")))) {
                if (result.length() > 0 && !result.toString().endsWith("\n\n") && !result.toString().endsWith("\n")) {
                    result.append("\n");
                }
                result.append(trimmed).append("\n");
                continue;
            }
            
            // 序号行：每个序号独立一行（匹配序号，可能没有空格）
            if (trimmed.matches("\\d+[.、].*") || trimmed.matches("[一二三四五六七八九十]+[、.].*")) {
                if (result.length() > 0 && !result.toString().endsWith("\n") && !result.toString().endsWith("\n\n")) {
                    result.append("\n");
                }
                result.append(trimmed);
                // 检查下一行，如果是序号或标题则换行
                if (i < lines.length - 1) {
                    String nextTrimmed = lines[i + 1].trim();
                    if (nextTrimmed.matches("\\d+[.、].*") || 
                        nextTrimmed.matches("[一二三四五六七八九十]+[、.].*") ||
                        nextTrimmed.contains("昨日处理主要工作") || 
                        nextTrimmed.contains("今日关注工作") ||
                        nextTrimmed.contains("交接班总结") ||
                        nextTrimmed.matches("\\d{4}-\\d{2}-\\d{2}.*") ||
                        nextTrimmed.isEmpty()) {
                        result.append("\n");
                    }
                } else {
                    result.append("\n");
                }
                continue;
            }
            
            // 普通内容行：如果上一行不是以换行结尾，添加换行
            if (result.length() > 0 && !result.toString().endsWith("\n") && !result.toString().endsWith("\n\n")) {
                result.append("\n").append(trimmed);
            } else {
                result.append(trimmed);
            }
        }
        
        // 清理多余的连续换行（超过2个的保留2个）
        String finalResult = result.toString().replaceAll("\\n{3,}", "\n\n");
        
        return finalResult.trim();
    }
}

