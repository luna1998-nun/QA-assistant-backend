# TTS 服务集成到 Java 后端开发文档

## 项目概述

本文档记录了将 MeloTTS (Python) 服务集成到现有 Java Spring Boot 后端的完整实现过程。

## 集成架构

```
前端 (Vue/Vite)
    ↓ HTTP请求
Java 后端 (Spring Boot) - 端口 8123
    ↓ 转发请求
Python TTS 服务 (FastAPI) - 端口 8001
    ↓ 调用模型
MeloTTS 模型 (CPU/GPU)
```

## 技术栈

- **前端**: Vue 3 + Vite + Naive UI
- **后端**: Spring Boot 3 + Spring AI
- **TTS服务**: Python + FastAPI + MeloTTS
- **模型**: MeloTTS 中文语音合成模型

## 开发文件说明

### 1. Java 后端控制器

**文件路径**: `src/main/java/com/hupi/hupiaiagent/controller/TtsController.java`

**功能特性**:
- 提供 `/api/ai/tts/speak` 语音合成接口
- 提供 `/api/ai/tts/get_model` 模型信息接口
- 提供 `/api/ai/tts/health` 健康检查接口
- 支持请求转发到 Python TTS 服务
- 配置专用 RestTemplate 处理长时请求

**关键实现**:
```java
@RestController
@RequestMapping("/ai/tts")
public class TtsController {

    @Resource(name = "ttsRestTemplate")
    private RestTemplate restTemplate;

    @Value("${tts.python.service.url:http://localhost:8001}")
    private String pythonTtsServiceUrl;

    @PostMapping("/speak")
    public ResponseEntity<byte[]> speech(@RequestBody Map<String, Object> body) {
        // 转发请求到 Python TTS 服务
        // 处理音频文件响应
    }
}
```

### 2. RestTemplate 配置

**文件路径**: `src/main/java/com/hupi/hupiaiagent/config/RestTemplateConfig.java`

**配置说明**:
- 通用 RestTemplate：30秒连接超时，10分钟读取超时
- TTS专用 RestTemplate：30秒连接超时，15分钟读取超时
- 支持长时间语音合成任务

### 3. 前端代理配置

**文件路径**: `QA-assistant-frontend/vite.config.ts`

**代理规则**:
```typescript
proxy: {
  '/api/ai/tts': {
    target: 'http://localhost:8001',
    changeOrigin: true,
    // 直接转发到 Python 服务
  },
  '/api/ai/dispatch_app/chat': {
    target: 'http://10.59.51.36:8123/',
    changeOrigin: true,
    // Java 后端接口代理
  }
}
```

## API 接口规范

### 1. 语音合成接口

**请求**:
```
POST /api/ai/tts/speak
Content-Type: application/json

{
  "input": "要合成的文本内容",
  "speed": 1.0,
  "language": "ZH"
}
```

**响应**:
```
Content-Type: audio/wav

[二进制音频数据]
```

### 2. 模型信息接口

**请求**:
```
GET /api/ai/tts/get_model?model_uid=melotts
```

**响应**:
```json
{
  "model_uid": "melotts",
  "model_name": "MeloTTS",
  "description": "MeloTTS 文本转语音模型",
  "supported_languages": ["ZH", "EN"],
  "speaker_ids": ["ZH", "EN"],
  "input_format": "text",
  "output_format": "audio"
}
```

### 3. 健康检查接口

**请求**:
```
GET /api/ai/tts/health
```

**响应**:
```json
{
  "status": "healthy",
  "service": "TTS Controller",
  "python_service_url": "http://localhost:8001"
}
```

## Python TTS 服务

### 核心文件

**文件路径**: `tools/tts-server/main.py`

**主要功能**:
- FastAPI 服务，监听 8001 端口
- 模拟 Java 后端接口路径：`/api/ai/tts/speak`
- 支持 CPU 模式运行（兼容性考虑）
- 降级模式：模型加载失败时返回静音音频

**启动命令**:
```bash
cd tools/tts-server
source .tts/bin/activate
python main.py
```

### 虚拟环境

- Python 3.10
- 依赖包安装在 `tools/tts-server/.tts/` 目录
- 包含 MeloTTS、FastAPI 等必要库

## 部署配置

### 环境变量

```properties
# Python TTS 服务地址
tts.python.service.url=http://localhost:8001
```

### 端口分配

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端开发服务器 | 3000 | Vite 开发服务器 |
| Java 后端 | 8123 | Spring Boot 应用 |
| Python TTS 服务 | 8001 | FastAPI MeloTTS 服务 |

## 开发注意事项

### 1. 性能优化

- TTS 模型加载时间较长（首次启动约30-60秒）
- 建议使用 CPU 模式以提高兼容性
- 配置合理的超时时间（15分钟）

### 2. 错误处理

- Python 服务不可用时，前端可以处理降级响应
- 模型加载失败时返回静音音频
- 完善的日志记录便于问题排查

### 3. 安全考虑

- 仅允许内部服务调用
- 输入文本长度限制
- 定期清理临时音频文件

## 测试验证

### 1. Python 服务测试

```bash
# 检查服务状态
curl http://localhost:8001/api/ai/tts/health

# 测试语音合成
curl -X POST http://localhost:8001/api/ai/tts/speak \
  -H "Content-Type: application/json" \
  -d '{"input":"测试语音合成","language":"ZH"}' \
  --output test.wav
```

### 2. Java 后端测试

```bash
# 检查 Java 接口
curl http://localhost:8123/api/ai/tts/health

# 通过 Java 后端测试语音合成
curl -X POST http://localhost:8123/api/ai/tts/speak \
  -H "Content-Type: application/json" \
  -d '{"input":"通过Java后端测试","language":"ZH"}' \
  --output test_java.wav
```

## 故障排查

### 常见问题

1. **Python 服务启动失败**
   - 检查虚拟环境是否正确激活
   - 确认端口 8001 未被占用
   - 查看模型下载进度

2. **Java 连接 Python 服务失败**
   - 检查防火墙设置
   - 确认 Python 服务健康状态
   - 查看网络连通性

3. **前端无法访问 TTS 接口**
   - 检查 Vite 代理配置
   - 确认 CORS 设置
   - 查看浏览器控制台错误

### 日志查看

```bash
# Python 服务日志
# 服务启动时会在终端显示详细日志

# Java 应用日志
# 查看控制台输出或 application.log 文件

# 前端开发服务器日志
# 查看 Vite 开发服务器控制台
```

## 后续优化建议

1. **模型优化**: 支持更多语言和说话人
2. **缓存机制**: 实现音频结果缓存
3. **异步处理**: 支持大文本异步合成
4. **监控告警**: 添加服务健康监控
5. **性能调优**: 优化模型加载和推理速度