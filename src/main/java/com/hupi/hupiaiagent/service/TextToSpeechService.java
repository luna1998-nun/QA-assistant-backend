package com.hupi.hupiaiagent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.audio.Audio;
import org.springframework.ai.audio.AudioFormat;
import org.springframework.ai.dashscope.audio.speech.DashScopeSpeechSynthesis;
import org.springframework.ai.dashscope.audio.speech.DashScopeSpeechSynthesisOptions;
import org.springframework.ai.dashscope.audio.speech.SpeechSynthesisPrompt;
import org.springframework.ai.dashscope.audio.speech.SpeechSynthesisResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文字转语音服务实现
 * 使用DashScope TTS实现文字转语音功能
 * 实现ITtsService接口，为后期本地化部署做准备
 */
@Service
public class TextToSpeechService implements ITtsService {

    private static final Logger logger = LoggerFactory.getLogger(TextToSpeechService.class);

    @Autowired
    private DashScopeSpeechSynthesis speechSynthesis;

    @Value("${spring.ai.dashscope.audio.speech.model:cosyvoice-v1}")
    private String model;

    @Value("${spring.ai.dashscope.audio.speech.voice.name:longwan}")
    private String defaultVoice;

    @Value("${spring.ai.dashscope.audio.speech.voice.language:chinese}")
    private String defaultLanguage;

    @Value("${spring.ai.dashscope.audio.speech.format:mp3}")
    private String defaultFormat;

    @Value("${spring.ai.dashscope.audio.speech.sample-rate:24000}")
    private Integer sampleRate;

    @Value("${spring.ai.dashscope.audio.speech.speed:1.0}")
    private Float speed;

    @Value("${spring.ai.dashscope.audio.speech.volume:1.0}")
    private Float volume;

    // 音频文件临时存储目录
    private static final String AUDIO_TEMP_DIR = System.getProperty("java.io.tmpdir") + "/tts-audio/";

    static {
        // 创建临时目录
        try {
            Files.createDirectories(Paths.get(AUDIO_TEMP_DIR));
        } catch (IOException e) {
            logger.error("Failed to create TTS audio temp directory: {}", AUDIO_TEMP_DIR, e);
        }
    }

    /**
     * 将文字转换为语音并保存为文件
     *
     * @param text 要转换的文字
     * @return 音频文件路径
     */
    public String textToSpeechFile(String text) {
        return textToSpeechFile(text, defaultVoice, defaultLanguage, defaultFormat);
    }

    /**
     * 将文字转换为语音并保存为文件（自定义音色）
     *
     * @param text 要转换的文字
     * @param voice 音色名称
     * @param language 语言
     * @param format 音频格式
     * @return 音频文件路径
     */
    public String textToSpeechFile(String text, String voice, String language, String format) {
        try {
            logger.info("Converting text to speech: text length={}, voice={}, language={}, format={}",
                       text.length(), voice, language, format);

            // 创建TTS请求选项
            DashScopeSpeechSynthesisOptions options = DashScopeSpeechSynthesisOptions.builder()
                    .withModel(model)
                    .withVoice(voice)
                    .withLanguage(language)
                    .withFormat(AudioFormat.valueOf(format.toUpperCase()))
                    .withSampleRate(sampleRate)
                    .withSpeed(speed)
                    .withVolume(volume)
                    .build();

            // 创建语音合成请求
            SpeechSynthesisPrompt prompt = new SpeechSynthesisPrompt(text, options);

            // 调用DashScope TTS API
            SpeechSynthesisResponse response = speechSynthesis.call(prompt);

            // 检查响应结果
            if (response.getResult() == null || response.getResult().getAudio() == null) {
                logger.error("TTS response is null or empty");
                return null;
            }

            Audio audio = response.getResult().getAudio();
            byte[] audioData = audio.getData();

            if (audioData == null || audioData.length == 0) {
                logger.error("TTS audio data is null or empty");
                return null;
            }

            // 生成唯一的文件名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("tts_%s_%s.%s", timestamp, UUID.randomUUID().toString().substring(0, 8), format);
            Path filePath = Paths.get(AUDIO_TEMP_DIR + fileName);

            // 保存音频文件
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                fos.write(audioData);
                fos.flush();
            }

            logger.info("Successfully saved TTS audio file: {}", filePath.toString());
            logger.info("Audio file size: {} bytes", audioData.length);

            return filePath.toString();

        } catch (Exception e) {
            logger.error("Failed to convert text to speech", e);
            return null;
        }
    }

    /**
     * 将文字转换为语音并返回字节数组
     *
     * @param text 要转换的文字
     * @return 音频数据字节数组
     */
    public byte[] textToSpeechBytes(String text) {
        return textToSpeechBytes(text, defaultVoice, defaultLanguage, defaultFormat);
    }

    /**
     * 将文字转换为语音并返回字节数组（自定义音色）
     *
     * @param text 要转换的文字
     * @param voice 音色名称
     * @param language 语言
     * @param format 音频格式
     * @return 音频数据字节数组
     */
    public byte[] textToSpeechBytes(String text, String voice, String language, String format) {
        try {
            logger.info("Converting text to speech bytes: text length={}, voice={}, language={}, format={}",
                       text.length(), voice, language, format);

            // 创建TTS请求选项
            DashScopeSpeechSynthesisOptions options = DashScopeSpeechSynthesisOptions.builder()
                    .withModel(model)
                    .withVoice(voice)
                    .withLanguage(language)
                    .withFormat(AudioFormat.valueOf(format.toUpperCase()))
                    .withSampleRate(sampleRate)
                    .withSpeed(speed)
                    .withVolume(volume)
                    .build();

            // 创建语音合成请求
            SpeechSynthesisPrompt prompt = new SpeechSynthesisPrompt(text, options);

            // 调用DashScope TTS API
            SpeechSynthesisResponse response = speechSynthesis.call(prompt);

            // 检查响应结果
            if (response.getResult() == null || response.getResult().getAudio() == null) {
                logger.error("TTS response is null or empty");
                return null;
            }

            Audio audio = response.getResult().getAudio();
            byte[] audioData = audio.getData();

            if (audioData == null || audioData.length == 0) {
                logger.error("TTS audio data is null or empty");
                return null;
            }

            logger.info("Successfully generated TTS audio bytes: {} bytes", audioData.length);
            return audioData;

        } catch (Exception e) {
            logger.error("Failed to convert text to speech bytes", e);
            return null;
        }
    }

    /**
     * 清理过期的音频文件
     *
     * @param hoursOld 清理多少小时前的文件
     */
    public void cleanupOldAudioFiles(int hoursOld) {
        try {
            Path tempDir = Paths.get(AUDIO_TEMP_DIR);
            if (!Files.exists(tempDir)) {
                return;
            }

            long cutoffTime = System.currentTimeMillis() - (hoursOld * 60 * 60 * 1000L);

            Files.list(tempDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            logger.info("Deleted old TTS audio file: {}", path.getFileName());
                        } catch (IOException e) {
                            logger.warn("Failed to delete old TTS audio file: {}", path.getFileName(), e);
                        }
                    });

        } catch (IOException e) {
            logger.error("Failed to cleanup old TTS audio files", e);
        }
    }

    @Override
    /**
     * 获取支持的音色列表
     *
     * @return 音色信息
     */
    public String getSupportedVoices() {
        return "{\n" +
                "  \"chinese\": [\"longwan\", \"xiaoqing\", \"xiaomeng\", \"xiaoxue\", \"xiaoyun\"],\n" +
                "  \"english\": [\"anna\", \"brian\", \"cathy\", \"david\", \"emily\"],\n" +
                "  \"japanese\": [\"haruka\", \"hikari\", \"kaori\", \"mai\", \"nana\"],\n" +
                "  \"korean\": [\"jihoon\", \"sujin\", \"yuna\", \"minjun\", \"hyejin\"],\n" +
                "  \"cantonese\": [\"xiaomin\", \"xiaofen\", \"xiaoya\", \"xiaoying\", \"xiaomei\"]\n" +
                "}";
    }

    @Override
    /**
     * 检查TTS服务状态
     *
     * @return 服务状态信息
     */
    public TtsServiceStatus getServiceStatus() {
        TtsServiceStatus status = new TtsServiceStatus(
                "DashScope TTS Service",
                "running",
                "DashScope",
                model
        );

        // 检查临时目录
        String tempDir = AUDIO_TEMP_DIR;
        File dir = new File(tempDir);
        status.setTempDirectory(tempDir);
        status.setTempDirectoryExists(dir.exists());

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            int fileCount = files != null ? files.length : 0;
            status.setTempFileCount(fileCount);
        } else {
            status.setTempFileCount(0);
        }

        status.setVersion("1.0.0");
        status.setDescription("DashScope CosyVoice TTS Implementation");

        return status;
    }
}