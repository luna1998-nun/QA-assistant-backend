package com.hupi.hupiaiagent.util;

/**
 * 实时流式格式化器（逐字实时格式化，无缓冲）
 * 检测格式前缀并立即添加换行标记
 */
public class RealtimeStreamFormatter {
    
    private boolean hasOutputNote = false;
    private StringBuilder contextWindow = new StringBuilder(); // 用于检测模式的小窗口（最后50个字符）
    
    /**
     * 处理新的chunk，逐字实时格式化，立即返回
     */
    public String processChunk(String chunk) {
        if (shouldSkipChunk(chunk)) {
            return "";
        }
        
        if (chunk == null || chunk.isEmpty()) {
            return "";
        }
        
        // 实时格式化：检测关键模式并添加换行标记
        StringBuilder formatted = new StringBuilder();
        
        // 更新上下文窗口
        contextWindow.append(chunk);
        if (contextWindow.length() > 50) {
            contextWindow.delete(0, contextWindow.length() - 50);
        }
        
        // 逐字符处理
        for (int i = 0; i < chunk.length(); i++) {
            char c = chunk.charAt(i);
            // 构建当前上下文（包含当前字符之前的内容）
            int contextLen = contextWindow.length() - chunk.length() + i;
            String context = contextLen > 0 ? contextWindow.substring(Math.max(0, contextLen - 25)) : "";
            
            // 检测序号前缀（如 "1." 或 "2、"）- 在 "." 或 "、" 前添加换行
            if (isNumberedPrefix(context, c)) {
                formatted.append('\n');
            }
            
            // 检测标题前缀
            if (isTitlePrefix(context, c)) {
                formatted.append('\n');
            }
            
            formatted.append(c);
        }
        
        return formatted.toString();
    }
    
    /**
     * 检测序号前缀（如 "1." 或 "2、"）
     * 在 "." 或 "、" 前添加换行（如果前面是数字且不是小数）
     */
    private boolean isNumberedPrefix(String context, char currentChar) {
        // 检测模式：当前字符是 "." 或 "、"，且前面是数字
        if (currentChar == '.' || currentChar == '、') {
            if (context.length() >= 1) {
                char prevChar = context.charAt(context.length() - 1);
                
                // 前面必须是数字
                if (Character.isDigit(prevChar)) {
                    // 检查更前面的字符，确保不是小数（如 "10.5"）
                    if (context.length() == 1) {
                        // 上下文只有1个字符（数字），可能是序号开头
                        // 但由于无法确认后面是什么，保守处理：不添加换行
                        return false;
                    }
                    
                    // 检查前前一个字符
                    char prevPrevChar = context.charAt(context.length() - 2);
                    
                    // 如果前前一个字符不是数字，说明是单个数字序号（如 "1."）
                    if (!Character.isDigit(prevPrevChar)) {
                        // 检查前面是否有换行或特定关键字（如 "主要工作"、"关注工作"）
                        boolean hasNewline = false;
                        boolean hasTitleKeyword = false;
                        
                        // 检查前面是否有换行
                        for (int i = context.length() - 3; i >= 0 && i >= context.length() - 25; i--) {
                            if (context.charAt(i) == '\n') {
                                hasNewline = true;
                                break;
                            }
                        }
                        
                        // 检查前面是否有标题关键字（说明序号紧跟标题）
                        String contextStr = context.substring(Math.max(0, context.length() - 20));
                        if (contextStr.contains("主要工作") || contextStr.contains("关注工作") || 
                            contextStr.contains("交接班总结")) {
                            hasTitleKeyword = true;
                        }
                        
                        // 如果前面有换行或标题关键字，说明是序号
                        if (hasNewline || hasTitleKeyword) {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * 检测标题前缀
     */
    private boolean isTitlePrefix(String context, char currentChar) {
        // 检测特定关键字开头
        // 由于是逐字处理，在关键字第一个字符前添加换行
        // 更严格的检测：只在行首或换行后检测
        
        // 检测 "昨日处理主要工作" - 在"昨"字前添加换行
        if (currentChar == '昨') {
            // 只在上下文为空、以换行结尾、或前面是日期格式时触发
            if (context.isEmpty() || context.endsWith("\n") || 
                context.matches(".*\\d{4}-\\d{2}-\\d{2}\\s*$")) {
                // 确保不在已存在的标题中
                if (!context.contains("昨日处理主要工作")) {
                    return true;
                }
            }
        }
        
        // 检测 "今日关注工作" - 在"今"字前添加换行
        if (currentChar == '今') {
            // 只在上下文为空、以换行结尾时触发
            if (context.isEmpty() || context.endsWith("\n")) {
                // 确保不在已存在的标题中
                if (!context.contains("今日关注工作")) {
                    return true;
                }
            }
        }
        
        
        return false;
    }
    
    /**
     * 判断是否应该跳过chunk
     */
    private boolean shouldSkipChunk(String chunk) {
        if (chunk == null || chunk.isEmpty() || hasOutputNote) {
            return true;
        }
        
        if (chunk.contains("输出说明") || chunk.contains("格式要求") || chunk.contains("注意事项")) {
            hasOutputNote = true;
            return true;
        }
        
        return false;
    }
    
    /**
     * 处理结束时返回剩余内容
     */
    public String finish() {
        // 无缓冲，直接返回空
        contextWindow.setLength(0);
        return "";
    }
    
    /**
     * 重置格式化器状态（用于新的会话）
     */
    public void reset() {
        contextWindow.setLength(0);
        hasOutputNote = false;
    }
}
