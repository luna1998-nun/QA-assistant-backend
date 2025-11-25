package com.hupi.hupiaiagent.service;

/**
 * TTS（文字转语音）服务接口
 * 提供统一的TTS服务抽象，支持多种TTS实现
 */
public interface ITtsService {

    /**
     * 将文字转换为语音并保存为文件
     *
     * @param text 要转换的文字
     * @return 音频文件路径
     */
    String textToSpeechFile(String text);

    /**
     * 将文字转换为语音并保存为文件（自定义参数）
     *
     * @param text 要转换的文字
     * @param voice 音色名称
     * @param language 语言
     * @param format 音频格式
     * @return 音频文件路径
     */
    String textToSpeechFile(String text, String voice, String language, String format);

    /**
     * 将文字转换为语音并返回字节数组
     *
     * @param text 要转换的文字
     * @return 音频数据字节数组
     */
    byte[] textToSpeechBytes(String text);

    /**
     * 将文字转换为语音并返回字节数组（自定义参数）
     *
     * @param text 要转换的文字
     * @param voice 音色名称
     * @param language 语言
     * @param format 音频格式
     * @return 音频数据字节数组
     */
    byte[] textToSpeechBytes(String text, String voice, String language, String format);

    /**
     * 清理过期的音频文件
     *
     * @param hoursOld 清理多少小时前的文件
     */
    void cleanupOldAudioFiles(int hoursOld);

    /**
     * 获取支持的音色列表
     *
     * @return 音色信息JSON字符串
     */
    String getSupportedVoices();

    /**
     * 检查TTS服务状态
     *
     * @return 服务状态信息
     */
    TtsServiceStatus getServiceStatus();
}