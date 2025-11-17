# AI Assistant 前后端对接文档

## 概述

前端 AI Assistant 已成功改造为调用后端 Spring Boot SSE 流式接口，实现了前后端分离架构。

## 后端接口说明

### 接口路径
- **基础路径**: `/api/ai/devOps_app/chat`
- **推荐接口**: `GET /api/ai/devOps_app/chat/sse_emitter`

### 接口参数
- `message` (string, required): 用户消息内容，需要进行 URL 编码
- `chatId` (string, required): 会话ID，用于维护对话历史

### 响应格式
使用 SSE (Server-Sent Events) 流式传输：

```
event: message
data: 文本片段1

event: message
data: 文本片段2

event: complete
data: Stream completed
```

### 其他可用接口
1. `GET /api/ai/devOps_app/chat/sync` - 同步响应（不推荐）
2. `GET /api/ai/devOps_app/chat/sse` - 简单 SSE 格式
3. `GET /api/ai/devOps_app/chat/server_sent_event` - 标准 SSE 格式

## 前端实现

### 1. 代理配置

在 `vite.config.ts` 中已添加：

```typescript
'/api/ai': {
  target: 'http://localhost:8123/',
  changeOrigin: true,
  secure: false,
}
```

### 2. API 调用层

**文件**: `src/api/assistant/chat.ts`

主要功能：
- `generateChatId()`: 生成唯一会话ID
- `sendMessageStream()`: 调用后端 SSE 接口
- SSE 流解析器：解析后端返回的 `message` 和 `complete` 事件

### 3. 组件改造

**文件**: `src/views/assistant/index.vue`

主要改动：
- 添加 `currentChatId` 状态管理会话ID
- 实现 `createNewSession()` 创建新会话
- 修改 `handleSend()` 传递会话ID和消息内容给后端

## 使用说明

### 启动前端
```bash
npm run dev
# 或
pnpm dev
```

### 确保后端服务运行
后端服务应运行在 `http://localhost:8123`

### 测试接口
1. 打开前端页面，进入 AI Assistant
2. 输入消息并发送
3. 查看控制台日志确认 SSE 连接成功
4. 观察流式响应效果

## 技术细节

### SSE 解析实现

前端使用 `fetch` + `ReadableStream` 解析 SSE 流：

```typescript
// 解析 SSE 格式
class SSEParser {
  parseChunk(chunk: string) {
    // 处理 event: 和 data: 行
    // 识别 message 和 complete 事件
  }
}
```

### 会话管理

每个对话会话都有唯一的 `chatId`：
- 格式：`chat-{timestamp}-{random}`
- 新建会话时自动生成新的 `chatId`
- 同一会话的所有消息共享相同的 `chatId`

### 错误处理

- 网络错误：显示在控制台
- 超时处理：后端配置 3 分钟超时
- 连接中断：自动清理失败的助手消息

## 注意事项

1. **URL 编码**: 消息内容会自动进行 URL 编码
2. **字符编码**: 使用 UTF-8 解码响应
3. **超时时间**: 后端 SSE 连接最长保持 3 分钟
4. **消息历史**: 由后端通过 `chatId` 维护会话历史

## 调试方法

### 查看网络请求
打开浏览器开发者工具 → Network 标签，筛选 `EventStream` 类型请求

### 查看日志
- 控制台日志：查看 SSE 连接状态和解析过程
- 后端日志：查看后端的 SSE 事件发送情况

## 后续优化建议

1. **错误提示**: 集成 Naive UI 的 `useMessage()` 实现更好的错误提示
2. **重连机制**: 实现 SSE 连接断线自动重连
3. **会话列表**: 在后端实现会话列表查询接口，用于会话切换
4. **消息持久化**: 前端支持消息本地缓存

## 常见问题

### Q: SSE 连接失败怎么办？
A: 检查：
1. 后端服务是否在运行
2. 代理配置是否正确
3. 后端接口路径是否正确

### Q: 为什么看不到流式响应？
A: 检查：
1. 后端是否返回 `text/event-stream` 类型
2. 控制台是否有解析错误
3. SSE 事件名是否为 `message`

### Q: 如何处理中文编码问题？
A: 前端使用 `UTF-8` 解码，后端确保响应使用 `UTF-8` 编码

## 更新记录

- **2025-01-17**: 完成前后端对接，实现 SSE 流式响应
- 移除直接调用 AI API 的代码
- 添加后端 SSE 接口调用
- 实现会话ID管理
- 添加完善的错误处理

