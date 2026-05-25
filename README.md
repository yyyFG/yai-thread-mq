# AI 代码生成与共享平台后端（异步化和RabbitMQ）

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.13-brightgreen" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Java-21-orange" alt="Java">
  <img src="https://img.shields.io/badge/Maven-Single--Module-blue" alt="Maven">
  <img src="https://img.shields.io/badge/Architecture-Monolith-success" alt="Architecture">
</p>

一个基于 Spring Boot 3、Java 21、LangChain4j 的 AI 代码生成与共享平台后端（单体版）。支持对话式生成代码、SSE 流式输出、代码解析与落盘、Vue 工程构建、应用部署、代码下载、聊天记录与网页截图等完整链路。

## 项目概述

本项目聚焦“用自然语言生成可运行项目代码”的后端能力建设，当前仓库为单体工程（所有能力在一个 Spring Boot 应用内），包含：

- **AI 对话生成**：通过 SSE 将生成过程实时推送给前端
- **代码工程化落地**：解析、保存、构建、下载与部署
- **任务解耦（可选）**：通过 RabbitMQ 将生成任务异步消费执行
- **平台基础设施**：登录态、权限校验、限流、缓存、统一异常处理等

## 核心特性

### AI 生成能力

- **流式生成（SSE）**：接口以 `text/event-stream` 实时返回生成内容
- **多生成模式**：支持 HTML / 多文件 / Vue 工程三种生成模式（见 `CodeGenTypeEnum`）
- **工具调用（Tools）**：内置文件读写、修改、删除、目录读取、退出等工具
- **结果解析与保存**：对生成结果进行解析并落盘到输出目录
- **Vue 工程构建**：支持对生成的 Vue 工程执行构建（`npm install` + `npm run build`）

### 应用能力

- **应用管理**：应用新增、更新、删除、查询
- **聊天记录**：保存用户与 AI 的对话历史并支持分页查询
- **代码下载**：将生成目录打包下载
- **应用部署**：将生成目录（Vue 工程使用 dist）复制到部署目录，并生成可访问 URL
- **网页截图**：部署后异步生成应用截图并上传对象存储

### 平台基础设施

- **统一响应与异常处理**：封装通用响应结构与全局异常处理器
- **权限控制**：注解鉴权与拦截器校验
- **限流保护**：基于 Redisson 的用户级限流
- **缓存与会话**：Redis + Spring Session 共享登录态

## 技术栈

- **Spring Boot 3.5.13** / **Java 21**
- **MyBatis-Flex 1.11.0**：数据访问层
- **MySQL 8.0+**：业务数据存储
- **Redis 6.0+**：Session / 缓存 / 限流
- **Redisson 3.50.0**：限流与 Redis 客户端能力
- **LangChain4j 1.1.x**：AI 模型接入与流式输出
- **RabbitMQ（可选）**：生成任务异步消费
- **Knife4j / SpringDoc**：OpenAPI 文档
- **Selenium / WebDriverManager**：网页截图
- **腾讯云 COS SDK**：图片上传

## 目录结构

```text
yai/
├── pom.xml
├── README.md
├── README_other.md
├── sql/                       # 数据库初始化脚本
├── src/main/java/cn/y/yai/
│   ├── ai/                    # AI 能力：模型配置、流式处理、工具、解析与保存、构建
│   ├── bizmq/                 # RabbitMQ：生产者/消费者/常量
│   ├── controller/            # 接口层（SSE 生成、部署、下载、聊天记录等）
│   ├── service/               # 业务服务
│   ├── mapper/                # MyBatis-Flex mapper
│   ├── ratelimiter/           # 限流组件（Redisson）
│   ├── utils/                 # 工具类（含截图工具）
│   └── YaiApplication.java    # 启动类
└── src/main/resources/
    ├── application.yml
    ├── application-local.yml
    ├── mapper/                # XML 映射
    └── prompt/                # Prompt 模板
```

## 环境要求

| 组件 | 版本要求 | 说明 |
|------|---------|------|
| Java | 21+ | Spring Boot 3 运行要求 |
| Maven | 3.9+ | 推荐本地 Maven |
| MySQL | 8.0+ | 业务数据库 |
| Redis | 6.0+ | Session、缓存、限流 |
| RabbitMQ | 3.11+ | 使用 MQ 生成链路时需要 |
| Chrome/Chromium | 最新版（可选） | 截图功能依赖（Linux 容器需安装依赖库） |

## 快速开始

### 1. 初始化数据库

创建数据库：

```sql
CREATE DATABASE yai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

导入脚本：

```bash
mysql -u root -p yai < sql/user.sql
mysql -u root -p yai < sql/app.sql
mysql -u root -p yai < sql/chat_history.sql
```

### 2. 修改配置

重点关注：

- `src/main/resources/application.yml`：MySQL / Redis / RabbitMQ / 端口与 context-path
- `src/main/resources/application-local.yml`：大模型 base-url / api-key / model-name
- 对象存储（COS）相关配置：建议使用环境变量或本地私有配置，不要提交密钥到仓库

### 3. 编译与启动

```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

默认服务地址：

- 服务：`http://localhost:8180/api`
- Knife4j：`http://localhost:8180/api/doc.html`

## 常用接口

- 应用代码流式生成（SSE）：
  `GET /api/app/chat/gen/code?appId={appId}&message={message}`
- 应用部署：
  `POST /api/app/deploy`
- 代码下载：
  `GET /api/app/download/{appId}`
- 聊天记录：
  `GET /api/chatHistory/app/{appId}`
- 静态资源预览：
  `GET /api/static/{dirName}/...`（目录名通常为 `{codeGenType}_{appId}`，例如 `html_1`）

## 开发说明

### 代码生成链路（核心）

1. `AppController.chatToCodeGen` 接收请求并返回 SSE 流
2. `AppServiceImpl` 创建/复用 appId 对应的推送通道（sink），并投递生成任务（可通过 MQ 异步消费）
3. `AiCodeGeneratorFacade` 负责生成并保存代码（HTML / 多文件 / Vue 工程）
4. `StreamHandlerExecutor` 负责对流式输出进行处理并写入聊天记录
5. 生成完成后更新应用状态，并根据需要触发构建/部署/截图等流程

### 输出目录

- 生成目录：`{user.dir}/tmp/code_output/{codeGenType}_{appId}`
- 部署目录：`{user.dir}/tmp/code_deploy/{deployKey}`

### 截图能力说明

截图依赖 Selenium + Chrome/Chromium。在 Linux 容器环境里，通常需要额外安装浏览器与系统依赖库；否则会出现 ChromeDriver 进程启动失败（如 exit code 127）。
