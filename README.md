# ZeroCode Backend

ZeroCode 后端项目，基于 Spring Boot 3 构建的现代化 Java 后端服务。

## 技术栈

- **框架**: Spring Boot 3.5.8
- **数据库**: MySQL
- **ORM**: MyBatis-Flex 1.11.0
- **连接池**: HikariCP
- **API 文档**: Knife4j 4.4.0 (OpenAPI 3)
- **工具库**: Hutool 5.8.38
- **AOP**: Spring AOP
- **开发工具**: Lombok

## 项目结构

```
src/main/java/com/xm/zerocodebackend/
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
│   └── JsonConfig.java       # JSON 配置
├── constant/            # 常量定义
│   └── UserConstant.java     # 用户常量
├── controller/          # 控制器
│   ├── HealthController.java # 健康检查
│   └── UserController.java   # 用户控制器
├── exception/           # 异常处理
│   ├── BusinessException.java  # 业务异常
│   ├── ErrorCode.java         # 错误码
│   ├── GlobalExceptionHandler.java  # 全局异常处理
│   └── ThrowUtils.java        # 异常工具
├── generator/           # 代码生成器
│   └── MyBatisCodeGenerator.java  # MyBatis 代码生成器
├── mapper/              # MyBatis 映射器
│   └── UserMapper.java   # 用户映射器
├── model/               # 数据模型
│   ├── dto/             # 数据传输对象
│   │   └── user/        # 用户相关 DTO
│   ├── entity/          # 实体类
│   │   └── User.java    # 用户实体
│   ├── enums/           # 枚举类
│   │   └── UserRoleEnum.java  # 用户角色枚举
│   └── vo/              # 视图对象
│       ├── LoginUserVO.java   # 登录用户视图
│       └── UserVO.java        # 用户视图
└── service/             # 服务层
    ├── UserService.java       # 用户服务接口
    └── impl/
        └── UserServiceImpl.java  # 用户服务实现
```

## 快速开始

### 环境要求
- Java 21+
- MySQL 8.0+
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
    url: jdbc:mysql://localhost:3306/zerocode
    username: your_username
    password: your_password
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
- `server.port`: 服务器端口 (默认: 8123)
- `server.servlet.context-path`: API 路径前缀 (默认: /api)
- `springdoc`: OpenAPI 文档配置
- `knife4j`: Knife4j 配置
- `xm.zerocode.salt`: 自定义盐值

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