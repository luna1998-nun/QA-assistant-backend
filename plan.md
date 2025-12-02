# MeloTTS集成开发计划

## 项目概述

在QA Assistant Backend项目中集成开源的MeloTTS模型，提供文本转语音功能。该系统基于Java 21 + Spring Boot 3.4.4架构，已集成多种AI服务和工具系统。

## 技术架构决策

### 集成方案：HTTP API + Docker容器化部署

**选择理由：**
- 解耦度高，Java和Python服务独立部署
- 利用现有HTTP基础设施和WebClient
- 易于扩展和维护，支持负载均衡
- 符合现有微服务架构模式

## 实施计划

### 阶段一：Python MeloTTS服务开发（1-2周）

#### 1.1 服务架构
```
melo-tts-service/
├── app.py                 # FastAPI主应用
├── requirements.txt       # Python依赖
├── Dockerfile            # Docker配置
├── models/               # MeloTTS模型目录
├── services/
│   ├── tts_service.py    # TTS核心服务
│   └── audio_processor.py # 音频处理
└── utils/audio_utils.py  # 音频工具函数
```

#### 1.2 核心API接口
- `POST /api/v1/tts/synthesis` - 文本转语音（同步）
- `GET /api/v1/tts/synthesis/{task_id}/stream` - 流式音频获取
- `POST /api/v1/tts/synthesis/stream` - 实时流式合成
- `GET /api/v1/tts/voices` - 获取可用音色列表
- `GET /api/v1/health` - 健康检查

### 阶段二：Java端TTS工具开发（1周）

#### 2.1 核心文件创建
1. **MeloTTSTool.java** - TTS工具类，提供@Tool注解的方法
2. **MeloTTSConfig.java** - 配置管理类
3. **TtsHttpClientService.java** - HTTP客户端服务
4. **TtsCacheManager.java** - 缓存管理器
5. **相关DTO类** - TtsRequest、TtsResponse、VoiceInfo

#### 2.2 关键代码结构
```java
@Component
public class MeloTTSTool {
    @Tool(description = "将文本转换为语音并生成音频文件")
    public String synthesizeSpeech(
        @ToolParam(description = "要转换的文本内容") String text,
        @ToolParam(description = "音色ID，默认为中文女声") String voiceId,
        @ToolParam(description = "音频格式：wav/mp3/ogg，默认wav") String format,
        @ToolParam(description = "语速0.5-2.0，默认1.0") Float speed
    );

    @Tool(description = "获取可用的TTS音色列表")
    public String getAvailableVoices();

    @Tool(description = "流式文本转语音，返回音频流URL")
    public String streamSpeech(
        @ToolParam(description = "要转换的文本内容") String text,
        @ToolParam(description = "音色ID") String voiceId
    );
}
```

### 阶段三：控制器集成和流式支持（1周）

#### 3.1 扩展AiController
在现有的`AiController.java`中添加TTS相关接口：

```java
/**
 * 文本转语音（同步）
 */
@PostMapping("/tts/synthesis")
public ResponseEntity<String> synthesizeTextToSpeech();

/**
 * 流式文本转语音
 */
@GetMapping(value = "/tts/stream", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
public ResponseEntity<Flux<DataBuffer>> streamTextToSpeech();

/**
 * SSE流式TTS（与现有AI对话集成）
 */
@GetMapping(value = "/dispatch_app/chat/sse_tts", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter doChatWithDispatchAppAndTTS(String message, String chatId, Boolean enableTTS);
```

#### 3.2 工具注册修改
在`ToolRegistration.java`中添加TTS工具到现有的工具注册中心。

### 阶段四：配置和部署（3-5天）

#### 4.1 应用配置
在`application.yml`中添加MeloTTS配置：

```yaml
melo:
  tts:
    service-url: http://localhost:8000
    timeout: 30000
    enable-cache: true
    cache-directory: ./tmp/tts-cache
    default-voice: zh-CN-female-1
    default-speed: 1.0
    default-format: wav
```

#### 4.2 Docker容器化部署
```yaml
services:
  melo-tts-service:
    build: ./melo-tts-service
    ports: ["8000:8000"]
    volumes: ["./models:/app/models"]

  qa-assistant-backend:
    build: .
    ports: ["8123:8123"]
    depends_on: [melo-tts-service]
    environment: ["MELO_TTS_SERVICE_URL=http://melo-tts-service:8000"]
```

## 关键技术挑战与解决方案

### 1. Java-Python互操作性
- **挑战**：HTTP通信稳定性、错误处理一致性
- **解决方案**：WebFlux WebClient + @Retryable重试机制 + @CircuitBreaker熔断器

### 2. 音频流式传输
- **挑战**：大文件传输内存管理、音频格式兼容性
- **解决方案**：Flux<DataBuffer>流式传输 + 音频格式适配器模式

### 3. 资源管理和性能优化
- **挑战**：音频文件缓存、并发处理、内存优化
- **解决方案**：Caffeine LRU缓存 + WebFlux异步处理 + 连接池管理

### 4. 多格式音频兼容性
- **挑战**：不同格式编解码、客户端兼容性
- **解决方案**：pydub格式转换 + Accept头自动选择格式

## 关键修改文件清单

### Java后端文件
1. **src/main/java/com/hupi/hupiaiagent/tools/MeloTTSTool.java** - 核心TTS工具类
2. **src/main/java/com/hupi/hupiaiagent/config/MeloTTSConfig.java** - 配置管理
3. **src/main/java/com/hupi/hupiaiagent/controller/AiController.java** - 控制器扩展
4. **src/main/java/com/hupi/hupiaiagent/tools/ToolRegistration.java** - 工具注册
5. **src/main/resources/application.yml** - 应用配置
6. **pom.xml** - 添加必要依赖

### Python服务文件
1. **melo-tts-service/app.py** - FastAPI主应用
2. **melo-tts-service/services/tts_service.py** - TTS核心服务
3. **melo-tts-service/Dockerfile** - Docker配置
4. **melo-tts-service/requirements.txt** - Python依赖

## 测试策略

### 单元测试
- Java端：Mockito + JUnit5测试TTS工具类
- Python端：pytest + FastAPI TestClient测试API接口

### 集成测试
- 端到端测试：验证AI对话+TTS完整流程
- 容器化测试：Docker Compose环境下的完整测试

### 性能测试
- 并发测试：模拟50个并发用户
- 验收标准：响应时间<2秒，成功率>99%

## 部署运维

### 生产部署架构
```
Load Balancer → API Gateway → QA Assistant App → MeloTTS Service → File Storage
```

### 监控指标
- 健康检查：HealthIndicator监控TTS服务状态
- 性能监控：响应时间、成功率、并发数
- 资源监控：CPU、内存、磁盘使用率

## 预期成果

### 功能特性
- 支持中英文文本转语音
- 支持多种音色和音频格式
- 集成到现有AI对话系统
- 支持音频缓存和流式传输

### 性能指标
- 单次合成时间：<2秒
- 支持50并发用户
- 内存使用稳定无泄漏

## 风险评估

### 技术风险
- **中等风险**：Python模型加载时间、网络通信稳定性
- **缓解措施**：模型预加载、重试机制、熔断器模式

### 运维风险
- **中等风险**：Python环境维护、资源管理
- **缓解措施**：容器化部署、监控告警、自动扩缩容

## 项目时间线

- **第1-2周**：Python MeloTTS服务开发和测试
- **第3周**：Java端TTS工具开发和集成
- **第4周**：控制器扩展和流式支持
- **第5周**：部署配置和性能优化

**总计：3-4周完成开发集成**

## 成功关键因素

1. **充分利用现有架构**：基于Spring AI工具系统扩展，避免大规模重构
2. **分阶段实施**：从基础功能开始，逐步完善高级特性
3. **充分测试验证**：重点关注性能和稳定性
4. **完善监控运维**：建立完善的监控和告警机制

该方案采用最小侵入性的集成方式，充分利用现有的Spring Boot + Spring AI架构，确保系统的可扩展性和可维护性。