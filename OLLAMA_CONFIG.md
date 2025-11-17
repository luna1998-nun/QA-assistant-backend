# Ollama服务器配置说明

## 当前配置

项目已配置为使用远程Ollama服务器：

```yaml
spring:
  ai:
    ollama:
      base-url: http://10.78.183.178:11434
      chat:
        model: deepseek-r1:8b
```

## 配置说明

- **服务器地址**：`http://10.78.183.178:11434`
- **模型名称**：`deepseek-r1:8b`
- **服务类型**：远程Ollama服务

## 启动应用

```bash
mvn spring-boot:run
```

## API测试

应用启动后，可以测试以下API：

### 基础对话测试
```bash
curl "http://localhost:8123/api/ai/devOps_app/chat/sync?message=你好&chatId=test123"
```

### 流式对话测试
```bash
curl "http://localhost:8123/api/ai/devOps_app/chat/sse?message=请介绍一下Linux系统&chatId=test123"
```

### 智能体测试
```bash
curl "http://localhost:8123/api/ai/manus/chat?message=帮我分析一下服务器性能问题"
```

## 注意事项

1. 确保网络能够访问 `http://10.78.183.178:11434`
2. 确保远程Ollama服务器正在运行
3. 确保服务器上已安装 `deepseek-r1:8b` 模型
4. 如果遇到连接问题，检查防火墙设置和网络连通性

## 故障排除

### 连接错误
- 检查网络连通性：`ping 10.78.183.178`
- 检查端口是否开放：`telnet 10.78.183.178 11434`
- 确认Ollama服务正在运行

### 模型未找到
- 确认服务器上已安装模型：`ollama list`
- 检查模型名称是否正确

### 应用启动失败
- 检查配置文件语法
- 查看应用日志获取详细错误信息
