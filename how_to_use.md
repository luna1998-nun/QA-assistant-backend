# TTS 项目使用操作指南

本文档详细说明如何启动 MeloTTS Python 服务、Java 后端服务和前端应用，以及 TTS 功能的集成原理。

## 📋 环境要求

### 系统要求
- **操作系统**: Linux/macOS/Windows (WSL2)
- **内存**: 最少 8GB RAM (推荐 16GB+)
- **存储**: 最少 5GB 可用空间
- **网络**: 稳定的互联网连接（用于模型下载）

### 软件依赖
- **Python**: 3.10+ (项目中使用 Python 3.10.19)
- **Java**: JDK 17+
- **Maven**: 3.6+
- **Node.js**: 16+
- **Git**: 最新版本

## 🚀 快速启动指南

### 步骤1: 启动 MeloTTS Python 服务

#### 1.1 进入 TTS 服务目录
```bash
cd /home/quipy/projects/QA-assistant-backend/tools/tts-server
```

#### 1.2 激活 Python 虚拟环境
```bash
source .tts/bin/activate
```
**说明**: 虚拟环境已预配置好，包含所有必需的依赖包。

#### 1.3 启动 TTS 服务
```bash
python main.py
```

**预期输出**:
```
[nltk_data] Error loading averaged_perceptron_tagger_eng: [可忽略的错误]
INFO:     Started server process [PID]
INFO:     Waiting for application startup.
INFO:     Application startup complete.
INFO:     Uvicorn running on http://0.0.0.0:8001 (Press CTRL+C to quit)
```

**重要提示**:
- 模型首次加载需要 30-60 秒
- 服务运行在 `http://localhost:8001`
- 使用 `Ctrl+C` 停止服务
- NLTK 错误可忽略，不影响 TTS 功能

#### 1.4 验证 TTS 服务
```bash
# 在新终端中测试
curl -s http://localhost:8001/api/ai/tts/health
```

### 步骤2: 启动 Java 后端服务

#### 2.1 进入项目根目录
```bash
cd /home/quipy/projects/QA-assistant-backend
```

#### 2.2 启动 Spring Boot 应用
```bash
mvn spring-boot:run
```

**预期输出**:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::               (v3.x.x)

...

Started HupiAiAgentApplication in X.XXX seconds
```

**重要提示**:
- Java 后端运行在 `http://localhost:8123`
- 确保 TTS Python 服务先启动
- 使用 `Ctrl+C` 停止服务

#### 2.3 验证 Java 后端 TTS 接口
```bash
# 测试 TTS 控制器健康状态
curl -s http://localhost:8123/api/ai/tts/health
```

### 步骤3: 启动前端应用

#### 3.1 进入前端目录
```bash
cd QA-assistant-frontend
```

#### 3.2 安装依赖（首次运行）
```bash
pnpm install
```

#### 3.3 启动开发服务器
```bash
pnpm dev
```

**预期输出**:
```
  VITE v5.x.x  ready in XXX ms

  ➜  Local:   http://localhost:3000/
  ➜  Network: http://192.168.x.x:3000/
  ➜  press h + enter to show help
```

**重要提示**:
- 前端运行在 `http://localhost:3000`
- 自动代理 TTS 请求到 Python 服务
- 自动代理其他请求到 Java 后端

#### 3.4 访问前端应用
打开浏览器访问: `http://localhost:3000`

## 🔧 完整启动脚本

### 方式1: 手动启动（推荐开发时使用）

```bash
# 终端1: 启动 Python TTS 服务
cd /home/quipy/projects/QA-assistant-backend/tools/tts-server
source .tts/bin/activate
python main.py

# 终端2: 启动 Java 后端
cd /home/quipy/projects/QA-assistant-backend
mvn spring-boot:run

# 终端3: 启动前端
cd /home/quipy/projects/QA-assistant-backend/QA-assistant-frontend
pnpm dev
```

### 方式2: 脚本自动化启动

创建启动脚本 `start_tts_system.sh`:

```bash
#!/bin/bash

# 设置项目根目录
PROJECT_ROOT="/home/quipy/projects/QA-assistant-backend"

echo "🚀 启动 TTS 系统..."

# 1. 启动 Python TTS 服务
echo "📢 启动 Python TTS 服务..."
cd "$PROJECT_ROOT/tools/tts-server"
source .tts/bin/activate
python main.py &
PYTHON_PID=$!
echo "Python TTS 服务 PID: $PYTHON_PID"

# 等待 Python 服务启动
sleep 30

# 2. 启动 Java 后端
echo "☕ 启动 Java 后端..."
cd "$PROJECT_ROOT"
mvn spring-boot:run &
JAVA_PID=$!
echo "Java 后端 PID: $JAVA_PID"

# 等待 Java 服务启动
sleep 60

# 3. 启动前端
echo "🌐 启动前端应用..."
cd "$PROJECT_ROOT/QA-assistant-frontend"
pnpm dev &
FRONTEND_PID=$!
echo "前端应用 PID: $FRONTEND_PID"

echo "✅ 所有服务已启动!"
echo "📢 Python TTS: http://localhost:8001 (PID: $PYTHON_PID)"
echo "☕ Java 后端: http://localhost:8123 (PID: $JAVA_PID)"
echo "🌐 前端应用: http://localhost:3000 (PID: $FRONTEND_PID)"
echo ""
echo "按 Ctrl+C 停止所有服务..."

# 等待用户中断
trap "echo '🛑 停止所有服务...'; kill $PYTHON_PID $JAVA_PID $FRONTEND_PID; exit" INT
wait
```

使用方法:
```bash
chmod +x start_tts_system.sh
./start_tts_system.sh
```

## 🧪 功能测试

### 测试1: 直接调用 Python TTS 服务

```bash
curl -X POST http://localhost:8001/api/ai/tts/speak \
  -H "Content-Type: application/json" \
  -d '{"input":"你好，这是测试语音合成","language":"ZH"}' \
  --output test_python.wav
```

### 测试2: 通过 Java 后端调用 TTS

```bash
curl -X POST http://localhost:8123/api/ai/tts/speak \
  -H "Content-Type: application/json" \
  -d '{"input":"通过Java后端测试语音合成","language":"ZH"}' \
  --output test_java.wav
```

### 测试3: 获取模型信息

```bash
# 从 Python 服务获取
curl "http://localhost:8001/get_model?model_uid=melotts"

# 从 Java 后端获取
curl "http://localhost:8123/api/ai/tts/get_model?model_uid=melotts"
```

### 测试4: 前端界面测试

1. 访问 `http://localhost:3000`
2. 找到 TTS 相关功能界面
3. 输入测试文本
4. 点击语音合成按钮
5. 验证音频播放

## 🛠️ 故障排查

### 问题1: Python TTS 服务启动失败

**症状**:
- 端口 8001 被占用
- 模型下载失败
- 虚拟环境问题

**解决方案**:
```bash
# 检查端口占用
lsof -i :8001
netstat -tlnp | grep 8001

# 杀死占用进程
sudo kill -9 <PID>

# 重新创建虚拟环境（如果需要）
cd tools/tts-server
rm -rf .tts
python -m venv .tts
source .tts/bin/activate
pip install -r requirements.txt
```

### 问题2: Java 后端连接 Python 服务失败

**症状**:
- Connection refused 错误
- 超时错误

**解决方案**:
```bash
# 检查 Python 服务状态
curl http://localhost:8001/api/ai/tts/health

# 检查防火墙设置
sudo ufw status

# 检查网络连通性
telnet localhost 8001
```

### 问题3: 前端无法访问 TTS 接口

**症状**:
- 浏览器控制台网络错误
- CORS 错误

**解决方案**:
```bash
# 检查 Vite 代理配置
cat QA-assistant-frontend/vite.config.ts | grep -A 10 "/api/ai/tts"

# 重启前端开发服务器
cd QA-assistant-frontend
npm run dev
```

## 📊 监控和日志

### 服务健康检查

```bash
# Python TTS 服务
curl -s http://localhost:8001/api/ai/tts/health | jq

# Java 后端 TTS 控制器
curl -s http://localhost:8123/api/ai/tts/health | jq

# Java 后端整体状态
curl -s http://localhost:8123/actuator/health | jq
```

### 日志查看

```bash
# Python 服务日志（在启动终端查看）
# 或后台运行时查看日志文件
tail -f /var/log/tts-service.log

# Java 应用日志
tail -f logs/application.log

# 前端开发服务器日志
# 在浏览器开发者工具中查看 Console
```

## 🔒 安全配置

### 生产环境配置

1. **更改默认端口**:
```properties
# application.properties
server.port=8123
tts.python.service.url=http://tts-service.internal:8001
```

2. **启用 HTTPS**:
```properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=your-password
```

3. **防火墙规则**:
```bash
# 只允许内部网络访问
sudo ufw allow from 10.0.0.0/8 to any port 8001
sudo ufw allow from 10.0.0.0/8 to any port 8123
```

## 📈 性能优化

### 1. TTS 服务优化

```python
# 在 main.py 中调整配置
import uvicorn

# 配置更多 worker 进程
uvicorn.run(app, host="0.0.0.0", port=8001, workers=4)
```

### 2. Java 后端优化

```yaml
# application.yml
spring:
  task:
    execution:
      pool:
        core-size: 10
        max-size: 50
        queue-capacity: 100
```

### 3. 前端优化

```typescript
// 在前端配置中启用缓存
const ttsConfig = {
  cacheEnabled: true,
  cacheExpiry: 3600000, // 1小时
  maxCacheSize: 100 // 最多缓存100个音频
}
```

## 🔄 更新和维护

### 更新 Python 依赖
```bash
cd tools/tts-server
source .tts/bin/activate
pip install --upgrade -r requirements.txt
```

### 更新 Java 依赖
```bash
mvn clean install
mvn dependency:updates
```

### 更新前端依赖
```bash
cd QA-assistant-frontend
npm update
```

## 📚 技术支持

### 常用命令速查

| 功能 | 命令 |
|------|------|
| 启动 Python TTS | `cd tools/tts-server && source .tts/bin/activate && python main.py` |
| 启动 Java 后端 | `mvn spring-boot:run` |
| 启动前端 | `cd QA-assistant-frontend && npm run dev` |
| 检查端口占用 | `lsof -i :8001` |
| 测试 TTS 接口 | `curl -X POST http://localhost:8001/api/ai/tts/speak -d '{"input":"测试","language":"ZH"}'` |
| 查看进程 | `ps aux \| grep -E "(python\|java\|node)"` |
| 杀死进程 | `pkill -f "python main.py"` |

---

## 🏗️ MeloTTS 功能集成架构解析

### 系统整体架构

本项目实现了一个三层架构的 TTS (Text-to-Speech) 语音合成系统：

```
前端层 (Vue 3 + Vite)
    ↓ HTTP/REST API
后端层 (Spring Boot + Java)
    ↓ HTTP/REST API
服务层 (Python + FastAPI + MeloTTS)
    ↓ Python 调用
模型层 (MeloTTS 深度学习模型)
```

### 前后端连接原理

#### 1. 前端代理机制 (Vite Dev Server)

前端通过 Vite 开发服务器的代理功能实现智能路由：

**配置文件**: `QA-assistant-frontend/vite.config.ts`

```typescript
proxy: {
  // TTS 请求直接路由到 Python 服务
  '/api/ai/tts': {
    target: 'http://localhost:8001',  // Python FastAPI 服务
    changeOrigin: true,
  },

  // 其他 AI 请求路由到 Java 后端
  '/api/ai/dispatch_app': {
    target: 'http://localhost:8123',   // Java Spring Boot
    changeOrigin: true,
  }
}
```

**工作流程**:
1. 前端发起请求 `/api/ai/tts/speak`
2. Vite 检测到 URL 匹配代理规则
3. 自动转发到 `http://localhost:8001/api/ai/tts/speak`
4. 对前端透明，无需修改代码

#### 2. Java 后端转发机制

Java 后端提供两种连接方式：

**方式A: 直接模式（开发阶段）**
- 前端 → Vite代理 → Python服务
- 跳过Java后端，减少延迟

**方式B: 转发模式（生产环境）**
- 前端 → Java后端 → Python服务
- 统一接口管理，便于监控和认证

**实现代码**: `TtsController.java`

```java
@PostMapping("/speak")
public ResponseEntity<byte[]> speech(@RequestBody Map<String, Object> body) {
    // 1. 接收前端请求
    // 2. 转发到 Python TTS 服务
    // 3. 处理音频响应
    // 4. 返回给前端
}
```

#### 3. 服务发现和配置

**配置文件**: `application.properties`

```properties
# Python TTS 服务地址（可配置）
tts.python.service.url=http://localhost:8001

# 连接超时配置
spring.web.client.connect-timeout=30s
spring.web.client.read-timeout=15m
```

### 关键技术实现

#### 1. RESTful API 设计

**统一的接口规范**:
- 所有服务都遵循 REST API 标准
- 统一的错误处理和响应格式
- 支持 CORS 跨域访问

**接口示例**:
```http
POST /api/ai/tts/speak
Content-Type: application/json

{
  "input": "要合成的文本",
  "speed": 1.0,
  "language": "ZH"
}
```

#### 2. 异步处理和超时管理

**Python FastAPI 端**:
```python
@app.post("/api/ai/tts/speak")
async def tts_generate(text: str):
    # 异步处理语音合成
    # 支持长文本和流式响应
```

**Java Spring Boot 端**:
```java
@Bean("ttsRestTemplate")
public RestTemplate ttsRestTemplate() {
    return builder
        .setReadTimeout(Duration.ofMinutes(15))  // 15分钟超时
        .build();
}
```

#### 3. 音频数据传输

**数据流**:
```
文本输入 → Python合成 → WAV音频 → 二进制传输 → 前端播放
```

**HTTP Headers**:
```http
Content-Type: audio/wav
Content-Length: [音频文件大小]
Cache-Control: max-age=3600
```

### 部署和扩展性

#### 1. 微服务架构优势

- **独立部署**: Python和Java服务可独立更新
- **弹性扩容**: 可根据负载独立扩展各服务
- **技术栈灵活**: 各服务可选择最适合的技术

#### 2. 负载均衡和高可用

```yaml
# 生产环境配置
tts:
  python:
    service:
      url: http://tts-cluster.internal:8001
      load-balancer: round-robin
      health-check: /health
```

#### 3. 缓存和优化

```java
// Java 后端缓存策略
@Cacheable(value = "tts-cache", key = "#text.hashCode()")
public byte[] generateSpeech(String text) {
    // 调用 Python TTS 服务
}
```

### 监控和运维

#### 1. 健康检查

每个服务都提供健康检查接口：
- Python: `/api/ai/tts/health`
- Java: `/api/ai/tts/health`
- 前端: 浏览器心跳检测

#### 2. 日志链路追踪

```java
// 请求ID传递，便于问题追踪
String requestId = UUID.randomUUID().toString();
log.info("TTS request started: {}", requestId);
// ... 处理请求 ...
log.info("TTS request completed: {}", requestId);
```

#### 3. 性能指标

关键指标监控：
- **延迟**: 端到端语音合成时间
- **吞吐量**: 每秒处理的请求数
- **成功率**: TTS 请求成功率
- **资源使用**: CPU、内存、GPU使用率

### 开发和调试

#### 1. 开发环境隔离

```bash
# 每个服务独立启动，便于调试
python main.py          # 端口8001
mvn spring-boot:run     # 端口8123
npm run dev            # 端口3000
```

#### 2. 热重载支持

- **Python**: 使用 `uvicorn --reload`
- **Java**: 使用 `spring-boot-devtools`
- **前端**: Vite 默认支持热重载

#### 3. 测试策略

```bash
# 单元测试
pytest tests/test_tts.py
mvn test
npm test

# 集成测试
curl -X POST http://localhost:8123/api/ai/tts/speak -d '{"input":"测试"}'
```

### 总结

这个 TTS 集成方案具有以下优势：

1. **架构清晰**: 三层分离，职责明确
2. **技术灵活**: 各层可选择最适合的技术栈
3. **易于维护**: 微服务架构，便于独立开发和部署
4. **性能优良**: 支持异步处理和负载均衡
5. **扩展性强**: 可轻松添加新的语言和语音模型
6. **监控完善**: 全链路监控和日志追踪

通过这种设计，项目既保持了高性能，又具备了企业级应用的可维护性和扩展性。