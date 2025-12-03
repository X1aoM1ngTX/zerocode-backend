# ZeroCode Backend

ZeroCode 后端项目，基于 Spring Boot 3 构建的现代化 Java 后端服务，集成 AI 代码生成功能。

## 技术栈

- **框架**: Spring Boot 3.5.8
- **数据库**: MySQL
- **ORM**: MyBatis-Flex 1.11.0
- **连接池**: HikariCP
- **缓存**: Redis + Spring Session
- **API 文档**: Knife4j 4.4.0 (OpenAPI 3)
- **工具库**: Hutool 5.8.38
- **AOP**: Spring AOP
- **开发工具**: Lombok
- **AI 集成**: LangChain4j 1.1.0
- **网页截图**: Selenium 4.33.0
- **云存储**: 腾讯云 COS 5.6.227
- **本地缓存**: Caffeine

## 项目结构

```
src/main/java/com/xm/zerocodebackend/
├── ai/                  # AI 代码生成模块
│   ├── model/           # AI 相关模型
│   │   ├── message/     # 消息模型
│   │   ├── HtmlCodeResult.java
│   │   └── MultiFileCodeResult.java
│   ├── tools/           # AI 工具类
│   ├── AiCodeGenTypeRoutingService.java
│   ├── AiCodeGenTypeRoutingServiceFactory.java
│   ├── AiCodeGeneratorService.java
│   └── AiCodeGeneratorServiceFactory.java
├── annotation/          # 自定义注解
│   └── AuthCheck.java   # 权限检查注解
├── aop/                 # AOP 切面
│   └── AuthInterceptor.java  # 权限拦截器
├── common/              # 通用类
│   ├── BaseResponse.java     # 统一响应格式
│   ├── DeleteRequest.java    # 删除请求
│   ├── PageRequest.java      # 分页请求
│   └── ResultUtils.java      # 响应工具类
├── config/              # 配置类
│   ├── CorsConfig.java       # 跨域配置
│   ├── CosClientConfig.java  # 腾讯云 COS 配置
│   ├── JsonConfig.java       # JSON 配置
│   ├── ReasoningStreamingChatModelConfig.java  # AI 流式模型配置
│   └── RedisChatMemoryStoreConfig.java  # Redis 聊天记忆配置
├── constant/            # 常量定义
│   ├── AppConstant.java      # 应用常量
│   └── UserConstant.java     # 用户常量
├── controller/          # 控制器
│   ├── AppController.java    # 应用控制器
│   ├── ChatHistoryController.java  # 聊天历史控制器
│   ├── HealthController.java # 健康检查
│   ├── StaticResourceController.java  # 静态资源控制器
│   └── UserController.java   # 用户控制器
├── core/                # 核心功能
│   ├── builder/         # 项目构建器
│   │   └── VueProjectBuilder.java
│   ├── handler/         # 流式处理器
│   ├── parser/          # 代码解析器
│   ├── saver/           # 代码保存器
│   ├── AiCodeGeneratorFacade.java
│   ├── CodeFileSaver.java
│   └── CodeParser.java
├── exception/           # 异常处理
│   ├── BusinessException.java  # 业务异常
│   ├── ErrorCode.java         # 错误码
│   ├── GlobalExceptionHandler.java  # 全局异常处理
│   └── ThrowUtils.java        # 异常工具
├── generator/           # 代码生成器
│   └── MyBatisCodeGenerator.java  # MyBatis 代码生成器
├── manager/             # 管理器
│   └── CosManager.java   # 腾讯云 COS 管理器
├── mapper/              # MyBatis 映射器
│   ├── AppMapper.java   # 应用映射器
│   ├── ChatHistoryMapper.java  # 聊天历史映射器
│   └── UserMapper.java   # 用户映射器
├── model/               # 数据模型
│   ├── dto/             # 数据传输对象
│   │   ├── app/         # 应用相关 DTO
│   │   ├── chatHistory/ # 聊天历史相关 DTO
│   │   └── user/        # 用户相关 DTO
│   ├── entity/          # 实体类
│   │   ├── App.java     # 应用实体
│   │   ├── ChatHistory.java  # 聊天历史实体
│   │   └── User.java    # 用户实体
│   ├── enums/           # 枚举类
│   │   ├── ChatHistoryMessageTypeEnum.java  # 聊天消息类型枚举
│   │   ├── CodeGenTypeEnum.java  # 代码生成类型枚举
│   │   └── UserRoleEnum.java  # 用户角色枚举
│   └── vo/              # 视图对象
│       ├── AppVO.java   # 应用视图
│       ├── LoginUserVO.java   # 登录用户视图
│       └── UserVO.java        # 用户视图
├── service/             # 服务层
│   ├── AppService.java  # 应用服务接口
│   ├── ChatHistoryService.java  # 聊天历史服务接口
│   ├── ProjectDownloadService.java  # 项目下载服务接口
│   ├── ScreenshotService.java  # 截图服务接口
│   ├── UserService.java  # 用户服务接口
│   └── impl/            # 服务实现
│       ├── AppServiceImpl.java
│       ├── ChatHistoryServiceImpl.java
│       ├── ProjectDownloadServiceImpl.java
│       ├── ScreenshotServiceImpl.java
│       └── UserServiceImpl.java
├── utils/               # 工具类
│   └── WebScreenshotUtils.java  # 网页截图工具
└── ZerocodeBackendApplication.java  # 应用程序入口
```

## 快速开始

### 环境要求
- Java 21+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

### 数据库配置
1. 创建数据库：
```sql
CREATE DATABASE zerocode CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行 SQL 脚本：
```bash
# 位于 src/main/resources/sql/create_user.sql
```

3. 修改配置文件 `src/main/resources/application.yml` 中的数据库连接信息：
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/zerocode
    username: your_username
    password: your_password
  # Redis 配置
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      password: 
      ttl: 3600
  # Session 配置
  session:
    store-type: redis
    timeout: 2592000  # 30 天过期
```

### 启动项目
1. 使用 Maven Wrapper：
```bash
./mvnw spring-boot:run
```

2. 或使用本地 Maven：
```bash
mvn spring-boot:run
```

3. 访问 API 文档：
```
http://localhost:8123/api/doc.html
```

## API 文档

项目集成了 Knife4j，提供了交互式的 API 文档。启动项目后访问：
- API 文档: http://localhost:8123/api/doc.html
- OpenAPI 规范: http://localhost:8123/api/v3/api-docs

## 核心功能

### AI 代码生成
项目集成了 LangChain4j，支持多种类型的代码生成：
- **HTML 代码生成**: 根据描述生成 HTML 页面
- **多文件代码生成**: 生成完整的项目结构，支持 Vue 项目
- **流式响应**: 支持实时流式输出，提升用户体验
- **工具集成**: 内置文件操作工具，支持代码的读取、写入和修改

### 权限控制
项目实现了基于注解的权限控制：
```java
@AuthCheck(mustRole = UserRoleEnum.ADMIN)
public Result<UserVO> getUserById(@PathVariable long id) {
    // 业务逻辑
}
```

### 统一响应格式
所有 API 响应都采用统一格式：
```java
{
    "code": 0,
    "data": {},
    "message": "操作成功"
}
```

### 全局异常处理
通过 `@ControllerAdvice` 实现全局异常处理，统一返回错误信息。

### 代码生成
使用 MyBatis-Flex 代码生成器快速生成实体和 Mapper：
```java
// 运行 MyBatisCodeGenerator
```

### 网页截图
集成 Selenium，支持网页截图功能：
- 自动化浏览器控制
- 高质量截图输出
- 支持多种浏览器

### 云存储
集成腾讯云 COS，支持文件存储和管理：
- 文件上传下载
- 安全访问控制
- 高可用性存储

## 开发指南

### 添加新功能
1. 在 `model/entity` 中创建实体类
2. 在 `mapper` 中创建 Mapper 接口
3. 在 `service` 中创建服务接口和实现
4. 在 `controller` 中创建控制器
5. 在 `model/dto` 和 `model/vo` 中创建传输对象

### 权限注解使用
```java
@AuthCheck(mustRole = UserRoleEnum.ADMIN)  // 需要管理员权限
@AuthCheck(mustRole = UserRoleEnum.USER)    // 需要用户权限
@AuthCheck(mustLogin = true)               // 需要登录
```

### 异常处理
```java
// 抛出业务异常
throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");

// 使用工具类
ThrowUtils.throwIf(condition, ErrorCode.PARAMS_ERROR);
```

## 配置说明

### application.yml 主要配置项
- `spring.datasource`: 数据库连接配置
- `spring.data.redis`: Redis 连接配置
- `spring.session`: Session 存储配置
- `server.port`: 服务器端口 (默认: 8123)
- `server.servlet.context-path`: API 路径前缀 (默认: /api)
- `springdoc`: OpenAPI 文档配置
- `knife4j`: Knife4j 配置
- `xm.zerocode.salt`: 自定义盐值

### AI 配置
- `langchain4j.openai`: OpenAI API 配置
- `langchain4j.community.redis`: Redis 聊天记忆配置

### 腾讯云 COS 配置
- `cos.region`: COS 地域
- `cos.secret-id`: 访问密钥 ID
- `cos.secret-key`: 访问密钥
- `cos.bucket`: 存储桶名称

## 部署

### 打包
```bash
./mvnw clean package
```

### 运行
```bash
java -jar target/zerocode-backend-0.0.1-SNAPSHOT.jar
```

### Docker 部署
```dockerfile
FROM openjdk:21-jdk-slim
COPY target/zerocode-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8123
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 测试

运行单元测试：
```bash
./mvnw test
```

## 常见问题

### 数据库连接失败
- 检查数据库服务是否启动
- 确认连接配置正确
- 检查防火墙设置

### API 文档无法访问
- 确认项目启动成功
- 检查端口是否被占用
- 访问正确的 URL 路径

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request