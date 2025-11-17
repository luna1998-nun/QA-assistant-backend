
# Hupi AI Agent

本项目是一款总结日志的 AI 智能助手，同时兼具 AI 技术应用与开发实践平台属性。项目以 Spring AI 为核心技术栈，搭建前后端分离架构。后端基于 Java 21+Spring Boot 3.4.4 分层构建，前端采用 Vue 3.5.18+Vite 7.1.2 开发，实现从大模型接入到智能体构建的完整链路，为开发者提供可直接落地的 AI 应用参考架构，数据存储采用 PGVector向量数据库，通过 RESTful API+SSE 异步推送保障服务通信与实时交互体验。

## 项目定位

本项目融合了运维专业知识与AI技术能力，既提供了开箱即用的AI运维工程师助手，又包含了完整的AI技术开发示例，适合以下场景：

- **运维团队**：快速获取运维问题诊断与解决方案
- **AI开发者**：学习大模型接入、RAG、Tool Calling等核心技术
- **技术团队**：作为内部AI应用平台的基础框架

## 核心特性

- **多模态大模型支持**：实现主流AI大模型的4种标准接入方式，覆盖云服务与本地部署
- **智能体架构**：基于ReAct模式构建的DevOps智能体，模拟10年+资深运维工程师经验
- **知识库增强**：完整的RAG实现，包含文档加载、向量化、检索优化全流程
- **工具调用系统**：灵活的Tool Calling机制，支持扩展自定义工具集
- **模型上下文管理**：MCP协议实现，确保多轮对话的上下文连续性
- **向量数据库集成**：PgVector与云数据库服务无缝整合，支持高效相似度检索
- **Prompt工程实践**：内置经过验证的提示词模板，优化模型输出质量
- **异步交互模式**：基于SSE实现的流式响应，提升用户体验

## 技术栈与架构

### 系统架构

项目采用前后端分离架构设计，确保系统的可扩展性和维护性：

- **后端**：基于Java 21和Spring Boot 3.4.4构建，采用分层设计模式
- **前端**：基于Vue 3.5.18和Vite 7.1.2开发，提供响应式用户界面
- **数据存储**：PostgreSQL (PGVector)作为向量数据库，支持高效相似度检索
- **服务通信**：RESTful API + SSE异步推送，保障实时交互体验

### 核心技术组件

| 类别 | 技术/框架 | 版本 | 用途 |
|------|----------|------|------|
| **基础框架** | Spring Boot | 3.4.4 | 后端应用框架 |
| | Java | 21 | 主要开发语言 |
| | Vue | 3.5.18 | 前端框架 |
| | Vue Router | 4.5.1 | 前端路由管理 |
| | Vite | 7.1.2 | 前端构建工具 |
| | Axios | 1.12.2 | HTTP客户端库 |
| **AI能力** | Spring AI | 1.0.0 | AI应用开发框架 |
| | Spring AI Alibaba | 1.0.0.2 | 阿里云AI集成 |
| | LangChain4j | 1.0.0-beta2 | 链式调用框架 |
| | PostgreSQL (PGVector) |  | 向量数据库 |
| | dashscope-sdk-java | 2.19.1 | 阿里云大模型SDK |
| **云服务** | 阿里云百炼 |  | 大模型平台 |
| | 阿里云PostgreSQL |  | 云数据库服务 |
| **工具集成** | SearchAPI |  | 网络搜索服务 |
| | Pexels API |             | 图片资源服务 |
| | Ollama |  | 本地大模型部署 |
| **开发工具** | Knife4j | 4.4.0 | API文档生成 |
| | Kryo | 5.6.2 | 高性能序列化 |
| | Jsoup | 1.19.1 | 网页解析与抓取 |
| | iText | 9.1.0 | PDF文档生成 |
| | Hutool | 5.8.37 | Java工具集 |
| | Lombok | 1.18.36 | 代码简化工具 |

## 核心功能模块

### 1. AI运维工程师

模拟10年+资深运维专家的能力与经验，涵盖Linux/Windows系统、数据库、网络、云原生等多领域运维知识，通过引导式问答模式，精准定位并解决各类运维问题。

### 2. 智能诊断与报告系统

基于常见运维场景构建故障诊断模型，结合用户提供的环境信息、现象描述、系统日志等数据，自动推理可能的故障原因并生成结构化解决方案报告。

### 3. RAG知识库系统

实现完整的检索增强生成流程，支持多种文档格式的批量导入、智能分块、向量化存储和语义检索，显著提升大模型在专业领域的知识准确性。

### 4. 工具调用框架

提供可扩展的工具调用系统，支持终端操作、文件读写、资源下载、PDF生成等实用功能，并提供统一的工具注册与管理机制。

### 5. 多模型适配层

设计统一的模型接口适配层，支持阿里云百炼、灵积等云服务模型，以及Ollama本地部署模型，实现无缝切换与组合调用。

## AI运维工程师功能详解

### 智能运维工作流

Hupi AI Agent 采用四阶段引导式诊断流程，模拟真实运维专家解决问题的思维过程：

1. **问题解构与初步分析**
   - 接收用户问题后，自动识别问题领域（系统/数据库/网络/云平台）
   - 提取关键信息，判断问题紧急程度和影响范围
   - 提供初步排查方向和临时缓解建议

2. **信息收集与问题聚焦**
   - 基于初步判断，生成3-5个针对性问题，覆盖环境配置、错误日志、系统状态等关键维度
   - 采用启发式引导，帮助用户梳理问题上下文，过滤无关信息
   - 动态调整后续问题，避免无效提问

3. **深入诊断与故障定位**
   - 综合用户反馈，构建问题场景模型
   - 结合RAG知识库和工具调用，验证假设并获取实时数据
   - 逐步缩小故障范围，定位根本原因

4. **方案生成与实施指导**
   - 提供详细的解决方案，包括分步操作指南、命令示例和配置文件模板
   - 标注高风险操作，并提供回滚策略
   - 解释方案原理，帮助用户理解问题本质

### 专业交互规范

系统遵循严格的专业运维交互原则，确保每一次回复都具备高价值：

- **技术严谨性**：所有建议基于成熟的运维实践和标准规范，避免经验主义
- **场景适配性**：根据不同的操作系统版本、软件环境提供针对性解决方案
- **可追溯性**：建议中包含版本号、命令参数说明，便于用户验证和排查
- **持续改进**：系统会记录问题解决过程，不断优化诊断准确率和方案质量

## 快速启动指南

### 环境准备

确保开发环境满足以下要求：

- **后端开发环境**：Java 21 (JDK 21.0.2+)、Maven 3.9.6+
- **前端开发环境**：Node.js 18.18+、npm 9.8+
- **数据库环境**：PostgreSQL 14+ (需安装PGVector扩展)
- **开发工具**：IntelliJ IDEA 2023.3+ (推荐) 或 Visual Studio Code (含Java、Vue扩展)

### 后端启动

1. 下载项目到本地

2. 安装后端依赖
```bash
cd hupi-ai-agent
mvn install
```

3. 配置API密钥和服务（见下方配置部分）

4. 启动后端服务
```bash
mvn spring-boot:run
```

后端服务将在 http://localhost:8123/api 启动

### 前端启动

1. 进入前端目录
```bash
cd hupi-ai-agent-frontend
```

2. 安装前端依赖
```bash
npm install
```

3. 启动前端服务
```bash
npm run dev
```

前端服务将在 http://localhost:5173 启动

## 配置指南

### 1. MCP服务配置

在 `target/classes/mcp-servers.json` 文件中配置百度MCP服务的API密钥：

```json
{
  "mcpServers": {
    "baidu-map": {
      "command": "npx.cmd",
      "args": ["-y", "@baidumap/mcp-server-baidu-map"],
      "env": {
        "BAIDU_MAP_API_KEY": "你的百度地图API密钥"
      }
    }
  }
}
```

**获取地址**：[百度地图API控制台](https://lbsyun.baidu.com/apiconsole/key)

### 2. 图片下载服务配置

图片下载服务使用Pexels图库，需要在相关工具类中配置API密钥。

**注册地址**：[Pexels官方网站](https://www.pexels.com/zh-cn/)

### 3. 联网搜索工具配置

在 `application.yml` 文件中配置SearchAPI的API密钥：

```yaml
## searchAPI
search-api:
  # 需要替换为你自己的 key
  api-key: 你的SearchAPI密钥
```

**注册地址**：[SearchAPI官方网站](https://www.searchapi.io/)

### 4. 大模型配置

在 `application.yml` 文件中配置阿里云百炼大模型的API密钥：

```yaml
spring:
  ai:
    dashscope:
      # 需要替换为你自己的 key
      api-key: 你的阿里云百炼API密钥
      chat:
        options:
          model: qwen-plus
```

**注册地址**：[阿里云百炼控制台](https://bailian.console.aliyun.com/)

### 5. RAG云数据库配置

在 `application.yml` 文件中配置阿里云PostgreSQL数据库信息：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://你的数据库地址:端口/数据库名
    username: 用户名
    password: 密码
  ai:
    vectorstore:
      pgvector:
        index-type: HNSW
        dimensions: 1536
        distance-type: COSINE_DISTANCE
        max-document-batch-size: 10000
```

**配置说明**：需要开通阿里云PostgreSQL服务，并配置外网访问地址和端口。不用时记得释放实例以节约成本。

**服务地址**：[阿里云PostgreSQL](https://www.aliyun.com/product/rds/postgresql)

## API文档

项目集成了Swagger和Knife4j，启动服务后可通过以下地址访问API文档：

- Swagger UI：http://localhost:8123/api/swagger-ui.html
- Knife4j UI：http://localhost:8123/api/doc.html

## 项目结构

```
hupi-ai-agent/
├── src/                        # 后端源码
│   ├── main/java/com/hupi/hupiaiagent/  # Java源代码目录
│   │   ├── agent/              # 智能体实现（HupiManus等）
│   │   ├── app/                # 应用实现（包含核心的DevOpsApp）
│   │   ├── tools/              # 运维工具集合
│   │   ├── rag/                # RAG知识库增强功能
│   │   ├── controller/         # API控制器（包含AI运维工程师相关接口）
│   │   └── demo/               # 示例代码
│   └── main/resources/         # 配置文件
├── hupi-ai-agent-frontend/     # 前端项目
│   ├── src/                    # 前端源码
│   │   ├── views/              # 页面组件（包含DevOpsApp.vue）
│   │   ├── components/         # 通用组件
│   │   └── router/             # 路由配置
│   └── public/                 # 静态资源
├── hupi-image-search-mcp-server/  # 图片搜索MCP服务
├── pom.xml                     # Maven项目配置
└── README.md                   # 项目说明文档
```

## 运维相关工具列表

AI运维工程师集成了多种实用工具，帮助解决各类运维场景问题：

| 工具名称 | 描述 | 配置需求 | 运维场景应用 |
|---------|------|---------|------------|
| WebSearchTool | 网页搜索工具，使用百度搜索引擎 | SearchAPI密钥 | 搜索最新的运维解决方案、错误排查方法、技术文档 |
| FileOperationTool | 文件操作工具，支持读写文件 | 无需额外配置 | 查看配置文件、保存日志、备份关键数据 |
| PDFGenerationTool | PDF生成工具 | 无需额外配置 | 生成运维报告、系统文档、配置说明 |
| ResourceDownloadTool | 资源下载工具 | 无需额外配置 | 下载补丁、驱动程序、配置模板 |
| TerminalOperationTool | 终端操作工具 | 无需额外配置 | 执行命令行操作、运行脚本、监控系统状态 |
| WebScrapingTool | 网页抓取工具 | 无需额外配置 | 收集技术文档、监控网站状态、获取配置指南 |
| MCP服务 | 百度地图服务 | 百度地图API密钥 | 网络拓扑分析、地理位置相关运维场景 |

## 注意事项

1. 所有API密钥请妥善保管，不要泄露在公共仓库中
2. 阿里云PostgreSQL服务在不使用时请及时释放，以避免产生额外费用
3. 开发环境下可以使用`application-local.yml`覆盖主配置文件的设置
4. 如需查看Spring AI的详细调用日志，请确保`application.yml`中的日志级别设置为DEBUG

