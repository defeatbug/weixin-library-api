# weixin-library-api

类微信读书的电子书阅读器后端 API。

## 技术栈

- **语言**: Java 21
- **框架**: Spring Boot 3.4.5
- **数据库**: PostgreSQL 17
- **缓存**: Redis 7
- **API**: GraphQL (Spring for GraphQL + GraphiQL)
- **ORM**: Spring Data JPA (Hibernate)
- **认证**: JWT
- **构建**: Maven

## 快速开始

### 前置条件

- Docker & Docker Compose
- 或本地安装 JDK 21 + PostgreSQL 17 + Redis 7

### 使用 DevContainer（推荐）

```bash
# 在 VS Code 中打开项目根目录，然后:
# "Reopen in Container"
# 容器启动后自动执行 mvn install
# 然后启动应用:
mvn spring-boot:run
```

### 使用 Docker Compose

```bash
# 启动所有服务
docker compose up -d

# 进入 devcontainer
docker compose exec devcontainer bash

# 在容器内构建并启动
mvn spring-boot:run
```

### 本地运行

```bash
# 启动 PostgreSQL 和 Redis
docker compose up -d postgresql redis

# 创建数据库（如尚未创建）
bash scripts/db-create.sh

# 启动应用
mvn spring-boot:run
```

### 配置

`.env` 文件（已提供默认值）：

| 变量 | 默认值 | 说明 |
|---|---|---|
| `POSTGRES_HOST` | `localhost` | PostgreSQL 主机 |
| `POSTGRES_PORT` | `5432` | PostgreSQL 端口 |
| `POSTGRES_USER` | `weixin` | 数据库用户 |
| `POSTGRES_PASSWORD` | `weixin123` | 数据库密码 |
| `POSTGRES_DB_NAME` | `weixin_library` | 数据库名 |
| `REDIS_HOST` | `localhost` | Redis 主机 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `APP_PORT` | `8080` | 应用端口 |

## 数据库管理

脚本放在 `scripts/` 目录下，需要在 devcontainer 内或安装了 `psql` 的环境中执行。

### 创建数据库

```bash
bash scripts/db-create.sh
```

幂等操作，数据库已存在时自动跳过。

### 删除数据库

```bash
bash scripts/db-drop.sh
```

非开发环境需设置 `DISABLE_DATABASE_ENVIRONMENT_CHECK=1` 确认。

### 重置数据库（删 + 建 + 种子数据）

```bash
bash scripts/db-reset.sh
```

依次执行：删除 → 创建 → 启动应用填充种子数据。

### 单独填充种子数据

```bash
bash scripts/db-seed.sh
```

应用启动时 `DataInitializer` 自动检测并填充缺失数据。

## 默认账号

| 邮箱 | 密码 | 角色 |
|---|---|---|
| `admin@weixin.library` | `admin123` | ADMIN |
| `user1@weixin.library` ~ `user10@weixin.library` | `123456` | USER |

应用启动后自动创建，已存在则跳过（幂等）。

## API

GraphQL 端点：`http://localhost:8080/graphql`

GraphiQL 调试界面：`http://localhost:8080/graphiql`

## 项目结构

```
src/main/java/com/weixinlibrary/
├── config/          # 配置类（Security, CORS, DataInitializer 等）
├── dto/             # 数据传输对象
├── entity/          # JPA 实体
├── exception/       # 全局异常处理
├── graphql/         # GraphQL Resolver
├── repository/      # JPA Repository
├── security/        # JWT 认证过滤器与工具
└── service/         # 业务逻辑层
```
