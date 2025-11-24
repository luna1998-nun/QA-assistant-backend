# TTS (文字转语音) 功能集成指南

## 功能概述

本项目已成功集成了TTS（Text-to-Speech）功能，实现了文字转语音的完整流程。用户可以通过设置面板配置语音参数，并在聊天界面中播放AI助手的语音回复。

## 技术架构

### 后端实现
- **服务接口**: `ITtsService` - 抽象TTS服务，支持后期本地化部署
- **实现类**: `TextToSpeechService` - 基于DashScope TTS的实现
- **控制器**: `TTSController` - 提供RESTful API接口
- **配置**: 启用DashScope Audio Speech自动配置

### 前端实现
- **钩子函数**: `useTTS` - TTS功能的核心逻辑
- **音频播放器**: `AudioPlayer` - 音频播放控制组件
- **设置面板**: `TTSSettings` - TTS参数配置界面
- **聊天集成**: 在`ChatWindow`组件中集成TTS功能

## 核心功能

### 1. 语音合成
- 支持中英文等多种语言
- 提供多种音色选择
- 支持MP3、WAV、PCM音频格式
- 可调节语速和音量

### 2. 音频播放控制
- 播放/暂停控制
- 进度条拖动定位
- 音量调节
- 音频文件下载

### 3. 用户配置
- 音色选择（中文女声/男声、英文等）
- 语速调节（0.5x - 2.0x）
- 音量调节（10% - 100%）
- 音频格式选择
- TTS开关控制

## API接口

### 1. 文字转语音文件
```
POST /api/tts/convert/file
Content-Type: application/json

{
  "text": "要转换的文字",
  "voice": "longwan",
  "language": "chinese",
  "format": "mp3"
}
```

### 2. 文字转语音流
```
POST /api/tts/convert/stream
Content-Type: application/json

{
  "text": "要转换的文字",
  "voice": "longwan",
  "language": "chinese",
  "format": "mp3"
}
```

### 3. 获取支持的音色
```
GET /api/tts/voices
```

### 4. 服务状态检查
```
GET /api/tts/status
```

### 5. 下载音频文件
```
GET /api/tts/download?filePath=xxx
```

## 配置说明

### 后端配置 (application.yml)
```yaml
spring:
  ai:
    dashscope:
      enabled: true
      api-key: ${DASHSCOPE_API_KEY:your-dashscope-api-key}
      audio:
        speech:
          enabled: true
          model: cosyvoice-v1
          voice:
            name: longwan
            language: chinese
          format: mp3
          sample-rate: 24000
          speed: 1.0
          volume: 1.0
```

### 环境变量
- `DASHSCOPE_API_KEY`: 阿里云百炼API密钥

## 使用流程

### 1. 启动应用
```bash
# 后端
mvn spring-boot:run

# 前端
npm run dev
```

### 2. 配置TTS
1. 点击聊天界面右上角的设置按钮
2. 在语音设置面板中：
   - 开启TTS功能
   - 选择喜欢的音色
   - 调节语速和音量
   - 选择音频格式

### 3. 使用语音功能
1. 与AI助手进行对话
2. AI回复后，点击"播放语音"按钮
3. 系统会生成语音并自动播放
4. 可以使用音频播放器控制播放进度
5. 支持下载音频文件

## 支持的音色

### 中文
- longwan (龙婉) - 默认女声
- xiaoqing (晓晴)
- xiaomeng (晓梦)
- xiaoxue (晓雪)
- xiaoyun (晓云)

### 英文
- anna, brian, cathy, david, emily

### 日文
- haruka, hikari, kaori, mai, nana

### 韩文
- jihoon, sujin, yuna, minjun, hyejin

### 粤语
- xiaomin, xiaofen, xiaoya, xiaoying, xiaomei

## 文件结构

```
src/main/java/com/hupi/hupiaiagent/
├── service/
│   ├── ITtsService.java              # TTS服务接口
│   ├── TextToSpeechService.java      # DashScope TTS实现
│   └── TtsServiceStatus.java         # 服务状态类
└── controller/
    └── TTSController.java            # TTS REST API控制器

QA-assistant-frontend/src/
├── components/
│   ├── AudioPlayer/                 # 音频播放器组件
│   └── TTSSettings/                 # TTS设置组件
├── hooks/
│   └── useTTS.ts                    # TTS功能钩子
└── views/assistant/components/
    └── ChatWindow.vue               # 集成TTS的聊天窗口
```

## 测试验证

### 1. 基础功能测试
- [x] TTS服务启动正常
- [x] 音色配置生效
- [x] 语音生成成功
- [x] 音频播放正常

### 2. 集成测试
- [x] 聊天界面TTS按钮显示
- [x] 语音生成加载状态
- [x] 音频播放器功能完整
- [x] 设置面板配置保存

### 3. 异常处理
- [x] 网络错误处理
- [x] 文本长度验证
- [x] API调用失败处理
- [x] 音频播放失败处理

## 性能优化

### 1. 音频文件管理
- 临时文件自动清理（默认24小时）
- 文件大小限制（文本最多5000字符）
- 支持流式传输减少内存占用

### 2. 用户体验
- 异步处理不阻塞UI
- 加载状态提示
- 错误信息友好显示
- 支持音频缓存

## 扩展性设计

### 1. 接口抽象
通过`ITtsService`接口抽象TTS服务，支持：
- 本地TTS引擎部署
- 其他云服务商TTS服务
- 自定义TTS模型集成

### 2. 配置灵活
- 支持多种音频格式
- 可配置音色参数
- 动态加载音色列表

## 故障排查

### 1. 常见问题
- **API密钥错误**: 检查`DASHSCOPE_API_KEY`环境变量
- **网络连接失败**: 确认可以访问阿里云服务
- **音频无法播放**: 检查浏览器音频格式支持
- **权限问题**: 确认临时目录有写入权限

### 2. 日志查看
```bash
# 查看TTS相关日志
grep "TTS\|tts" logs/application.log

# 检查音频文件目录
ls -la /tmp/tts-audio/
```

## 后续优化建议

1. **本地化部署**: 集成本地TTS引擎，减少网络依赖
2. **实时流式**: 实现音频流实时生成和播放
3. **语音克隆**: 支持用户自定义音色
4. **多语言支持**: 扩展更多语言和方言支持
5. **离线缓存**: 实现音频文件本地缓存机制

---

**开发完成时间**: 2025-11-25
**版本**: 1.0.0
**兼容性**: Vue 3 + Spring Boot 3.4