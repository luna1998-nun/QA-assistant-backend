package com.hupi.hupiaiagent.controller;

import com.hupi.hupiaiagent.service.ITtsService;
import com.hupi.hupiaiagent.service.TtsServiceStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 文字转语音控制器
 * 提供TTS（Text-to-Speech）相关的API接口
 * 通过ITtsService接口调用，支持多种TTS实现
 */
@RestController
@RequestMapping("/tts")
@Tag(name = "TTS接口", description = "文字转语音相关接口")
@Slf4j
public class TTSController {

    @Autowired
    private ITtsService ttsService;

    /**
     * 将文字转换为语音文件
     *
     * @param request 包含要转换文字的请求体
     * @return 音频文件路径
     */
    @PostMapping("/convert/file")
    @Operation(summary = "文字转语音文件", description = "将文字转换为音频文件并返回文件路径")
    public ResponseEntity<Map<String, Object>> textToSpeechFile(@RequestBody TTSRequest request) {
        log.info("TTS file request: text length={}, voice={}, language={}, format={}",
                request.getText().length(), request.getVoice(), request.getLanguage(), request.getFormat());

        if (request.getText() == null || request.getText().trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "文本内容不能为空");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        String audioFilePath = ttsService.textToSpeechFile(
                request.getText(),
                request.getVoice(),
                request.getLanguage(),
                request.getFormat()
        );

        Map<String, Object> response = new HashMap<>();
        if (audioFilePath != null) {
            response.put("success", true);
            response.put("filePath", audioFilePath);
            response.put("message", "语音合成成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "语音合成失败");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 将文字转换为语音并直接返回音频流
     *
     * @param request 包含要转换文字的请求体
     * @return 音频文件流
     */
    @PostMapping("/convert/stream")
    @Operation(summary = "文字转语音流", description = "将文字转换为音频流并直接返回")
    public ResponseEntity<Resource> textToSpeechStream(@RequestBody TTSRequest request) {
        log.info("TTS stream request: text length={}, voice={}, language={}, format={}",
                request.getText().length(), request.getVoice(), request.getLanguage(), request.getFormat());

        if (request.getText() == null || request.getText().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        byte[] audioData = ttsService.textToSpeechBytes(
                request.getText(),
                request.getVoice(),
                request.getLanguage(),
                request.getFormat()
        );

        if (audioData == null || audioData.length == 0) {
            return ResponseEntity.internalServerError().build();
        }

        ByteArrayResource resource = new ByteArrayResource(audioData);

        // 根据格式设置MIME类型
        MediaType mediaType = getMediaType(request.getFormat());

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tts_audio." + request.getFormat() + "\"")
                .body(resource);
    }

    /**
     * 下载音频文件
     *
     * @param filePath 音频文件路径
     * @return 音频文件
     */
    @GetMapping("/download")
    @Operation(summary = "下载音频文件", description = "根据文件路径下载音频文件")
    public ResponseEntity<Resource> downloadAudioFile(
            @Parameter(description = "音频文件路径") @RequestParam String filePath) {

        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new org.springframework.core.io.FileSystemResource(path.toFile());

            // 根据文件扩展名确定MIME类型
            String fileName = path.getFileName().toString();
            String format = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
            MediaType mediaType = getMediaType(format);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading audio file: {}", filePath, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取支持的音色列表
     *
     * @return 音色信息
     */
    @GetMapping("/voices")
    @Operation(summary = "获取支持的音色", description = "获取TTS支持的所有音色列表")
    public ResponseEntity<String> getSupportedVoices() {
        String voices = ttsService.getSupportedVoices();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(voices);
    }

    /**
     * 清理过期的音频文件
     *
     * @param hoursOld 清理多少小时前的文件，默认24小时
     * @return 清理结果
     */
    @PostMapping("/cleanup")
    @Operation(summary = "清理过期文件", description = "清理指定时间前的临时音频文件")
    public ResponseEntity<Map<String, Object>> cleanupOldFiles(
            @Parameter(description = "清理多少小时前的文件")
            @RequestParam(defaultValue = "24") int hoursOld) {

        log.info("Cleaning up TTS audio files older than {} hours", hoursOld);

        try {
            ttsService.cleanupOldAudioFiles(hoursOld);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "清理完成");
            response.put("hoursOld", hoursOld);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error cleaning up TTS audio files", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "清理失败: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 检查TTS服务状态
     *
     * @return 服务状态
     */
    @GetMapping("/status")
    @Operation(summary = "检查TTS服务状态", description = "检查TTS服务是否正常运行")
    public ResponseEntity<TtsServiceStatus> getTTSStatus() {
        TtsServiceStatus status = ttsService.getServiceStatus();
        return ResponseEntity.ok(status);
    }

    /**
     * 根据格式获取对应的MIME类型
     */
    private MediaType getMediaType(String format) {
        switch (format.toLowerCase()) {
            case "mp3":
                return MediaType.parseMediaType("audio/mpeg");
            case "wav":
                return MediaType.parseMediaType("audio/wav");
            case "pcm":
                return MediaType.parseMediaType("audio/pcm");
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    /**
     * TTS请求DTO
     */
    public static class TTSRequest {
        private String text;
        private String voice = "longwan";
        private String language = "chinese";
        private String format = "mp3";

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getVoice() {
            return voice;
        }

        public void setVoice(String voice) {
            this.voice = voice;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }
    }
}