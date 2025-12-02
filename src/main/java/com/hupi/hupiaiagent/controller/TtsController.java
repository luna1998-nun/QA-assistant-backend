package com.hupi.hupiaiagent.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/ai/tts")
@Slf4j
public class TtsController {

    @Resource(name = "ttsRestTemplate")
    private RestTemplate restTemplate;

    @Value("${tts.python.service.url:http://localhost:8001}")
    private String pythonTtsServiceUrl;

    /**
     * TTS语音合成接口
     *
     * @param body 请求体，包含input文本、speed、language等参数
     * @return 音频文件流
     */
    @PostMapping("/speak")
    public ResponseEntity<byte[]> speech(@RequestBody Map<String, Object> body) {
        try {
            log.info("Received TTS request: {}", body);

            // 验证必要参数
            if (!body.containsKey("input") || body.get("input") == null ||
                body.get("input").toString().trim().isEmpty()) {
                log.error("TTS request missing 'input' parameter");
                return ResponseEntity.badRequest().build();
            }

            // 构建转发到Python服务的URL
            String pythonUrl = pythonTtsServiceUrl + "/speech";
            log.info("Forwarding TTS request to Python service: {}", pythonUrl);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 创建请求实体
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            // 调用Python TTS服务
            ResponseEntity<byte[]> response = restTemplate.exchange(
                pythonUrl,
                HttpMethod.POST,
                entity,
                byte[].class
            );

            // 处理Python服务的响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("TTS request successful, audio size: {} bytes", response.getBody().length);

                // 返回音频文件，保持与Python服务一致的媒体类型
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/wav"))
                    .body(response.getBody());
            } else {
                log.error("TTS service returned error status: {}", response.getStatusCode());
                return ResponseEntity.status(response.getStatusCode()).build();
            }

        } catch (Exception e) {
            log.error("Error processing TTS request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取TTS模型信息接口
     *
     * @param model_uid 模型唯一标识符
     * @return 模型信息
     */
    @GetMapping("/get_model")
    public ResponseEntity<Map<String, Object>> getModel(@RequestParam String model_uid) {
        try {
            log.info("Received get_model request for model_uid: {}", model_uid);

            // 构建请求参数
            Map<String, String> params = new HashMap<>();
            params.put("model_uid", model_uid);

            // 构建转发到Python服务的URL
            String pythonUrl = pythonTtsServiceUrl + "/get_model?model_uid=" + model_uid;
            log.info("Forwarding get_model request to Python service: {}", pythonUrl);

            // 调用Python TTS服务
            ResponseEntity<Map> response = restTemplate.getForEntity(pythonUrl, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Get model request successful");
                return ResponseEntity.ok(response.getBody());
            } else {
                log.error("Get model request failed with status: {}", response.getStatusCode());
                return ResponseEntity.status(response.getStatusCode()).build();
            }

        } catch (Exception e) {
            log.error("Error processing get_model request", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 健康检查接口，用于检查TTS服务是否可用
     *
     * @return 服务状态
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        try {
            log.info("TTS health check requested");

            Map<String, Object> health = new HashMap<>();
            health.put("status", "healthy");
            health.put("service", "TTS Controller");
            health.put("python_service_url", pythonTtsServiceUrl);

            return ResponseEntity.ok(health);

        } catch (Exception e) {
            log.error("Error in TTS health check", e);
            Map<String, Object> health = new HashMap<>();
            health.put("status", "unhealthy");
            health.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(health);
        }
    }
}