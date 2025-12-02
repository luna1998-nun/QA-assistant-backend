# QA Assistant Backend 项目架构文档

## 项目概述

QA Assistant Backend 是一款基于 Spring AI 的智能运维助手后端服务，融合了 AI 技术与运维专业知识，提供多模型支持、智能体交互、RAG 知识库增强等核心功能。

## 技术栈

### 核心框架
- **Java 21** - 主要开发语言
- **Spring Boot 3.4.4** - 后端应用框架
- **Spring AI 1.0.0** - AI应用开发框架
- **Spring AI Alibaba 1.0.0.2** - 阿里云AI集成

### 数据库与存储
- **PostgreSQL (PGVector)** - 向量数据库
- **Maven** - 项目构建管理

### AI与大模型
- **阿里云百炼 (DashScope)** - 云服务大模型
- **Ollama** - 本地大模型部署
- **LangChain4j** - 链式调用框架

### 工具与集成
- **Knife4j 4.4.0** - API文档生成
- **Jsoup 1.19.1** - 网页解析
- **iText 9.1.0** - PDF生成
- **Hutool 5.8.37** - Java工具集
- **Kryo 5.6.2** - 高性能序列化

## 项目结构

```
hupi-ai-agent/
├── src/
│   ├── main/
│   │   ├── java/com/hupi/hupiaiagent/           # Java源代码根目录
│   │   │   ├── HupiAiAgentApplication.java     # 应用启动入口
│   │   │   ├── advisor/                        # 顾问模式实现
│   │   │   ├── agent/                          # 智能体实现
│   │   │   │   ├── HupiManus.java             # 核心运维智能体
│   │   │   │   └── model/                      # 智能体相关模型
│   │   │   ├── app/                           # 应用层核心功能
│   │   │   ├── chatmemory/                    # 对话记忆管理
│   │   │   ├── config/                        # 配置类
│   │   │   ├── constant/                      # 常量定义
│   │   │   ├── controller/                    # API控制器层
│   │   │   ├── demo/                          # 示例代码
│   │   │   │   ├── invoke/                    # 调用示例
│   │   │   │   └── rag/                       # RAG示例
│   │   │   ├── exception/                     # 异常处理
│   │   │   ├── rag/                           # RAG知识库增强功能
│   │   │   ├── tools/                         # 工具调用系统
│   │   │   │   ├── FileOperationTool.java    # 文件操作工具
│   │   │   │   ├── PDFGenerationTool.java    # PDF生成工具
│   │   │   │   ├── ResourceDownloadTool.java # 资源下载工具
│   │   │   │   ├── TerminalOperationTool.java # 终端操作工具
│   │   │   │   ├── WebScrapingTool.java      # 网页抓取工具
│   │   │   │   ├── WebSearchTool.java        # 网络搜索工具
│   │   │   │   └── ToolRegistration.java     # 工具注册管理
│   │   │   └── util/                          # 工具类
│   │   └── resources/                         # 配置文件资源
│   │       ├── application.yml               # 主配置文件
│   │       ├── application-local.yml         # 本地开发配置
│   │       └── mcp-servers.json              # MCP服务配置
│   └── test/                                  # 测试代码
├── QA-assistant-frontend/                     # 前端项目目录
├── image-search-mcp-server/                   # 图片搜索MCP服务
├── pom.xml                                    # Maven项目配置
├── mvnw                                       # Maven wrapper脚本
└── README.md                                  # 项目说明文档
```

## 核心模块架构

### 1. 智能体模块 (agent/)
- **HupiManus**: 基于ReAct模式构建的DevOps智能体，模拟资深运维工程师经验
- 支持多轮对话、工具调用、知识库检索等核心能力
- 实现问题解构、信息收集、深入诊断、方案生成的完整工作流

### 2. 工具调用系统 (tools/)
提供丰富的运维工具集合：
- **文件操作**: 文件读写、配置管理
- **终端操作**: 命令执行、脚本运行
- **网络工具**: 搜索、抓取、下载
- **文档生成**: PDF报告生成
- **MCP服务**: 地图服务等第三方集成

### 3. RAG知识库系统 (rag/)
- 完整的检索增强生成流程
- 支持多文档格式导入和智能分块
- 向量化存储和语义检索
- 多查询扩展优化

### 4. 对话记忆管理 (chatmemory/)
- 维护多轮对话上下文
- 支持长期记忆和短期记忆
- 基于MCP协议的模型上下文管理

### 5. 配置管理 (config/)
- 多环境配置支持
- AI模型配置（阿里云百炼、Ollama）
- 数据库配置（PGVector）
- 第三方服务API配置

## API设计

### RESTful API 接口
- 基于Spring Boot的控制器层设计
- 统一异常处理和响应格式
- 集成Knife4j API文档

### SSE流式响应
- 支持实时流式输出
- 提升用户交互体验
- 异步消息推送机制

## 部署架构

### 开发环境
- 本地Ollama模型部署
- 内存向量数据库支持
- 热重载开发模式

### 生产环境
- 阿里云PGVector向量数据库
- 阿里云百炼大模型服务
- 分布式部署支持

## 配置说明

### 核心配置文件
- `application.yml`: 主配置文件
- `application-local.yml`: 本地开发环境配置
- `mcp-servers.json`: MCP服务配置

### 关键配置项
- **AI模型配置**: 支持阿里云百炼和本地Ollama
- **数据库配置**: PGVector向量数据库连接
- **第三方服务**: 搜索API、地图API等
- **工具集成**: 各种运维工具的启用配置

## 扩展性设计

### 工具扩展
- 统一的工具注册机制
- 支持自定义工具开发
- 工具链组合调用

### 模型扩展
- 统一的模型接口适配
- 支持新模型快速接入
- 模型切换和负载均衡

### 功能扩展
- 模块化架构设计
- 插件式功能扩展
- 配置驱动的功能开关

## 安全考虑

- API密钥安全管理和配置
- 输入验证和输出过滤
- 工具调用权限控制
- 数据传输加密

## 监控与运维

- Spring Boot Actuator健康检查
- 应用性能监控
- 日志聚合和分析
- 错误追踪和告警