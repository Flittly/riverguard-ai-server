# RiverGuard AI Server — 长江重点岸段崩岸监测预警智能体 · 后端服务

[![Java](https://img.shields.io/badge/Java-21-blue)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.7-brightgreen)](https://spring.io/projects/spring-boot)
[![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.16-orange)](https://baomidou.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1)](https://www.postgresql.org/)

Backend service for the RiverGuard AI Platform. Provides RESTful APIs for user authentication, role-based access control, business data management, and geospatial analysis.

**RiverGuard AI Platform** 的后端服务，提供用户认证、基于角色的权限控制、业务数据管理和地理空间分析等 RESTful API。

---

## Tech Stack — 技术栈

| Category | Technology |
|----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.7, Spring Security 6 |
| ORM | MyBatis-Plus 3.5.16 (with `mybatis-plus-spring-boot4-starter`) |
| Database | PostgreSQL 16 |
| Auth | JWT (jjwt 0.12.6), BCrypt |
| Build | Maven 3.9+ |

## Project Structure — 项目结构

```
riverguard-ai-server/
├── riverguard-common/                    # Shared module
│   └── src/main/java/io/riverguard/
│       ├── common/api/                   # R<T>, PageResult, ResultCode
│       ├── common/exception/             # BusinessException, GlobalExceptionHandler
│       └── common/model/                 # BaseEntity, PageParam
├── riverguard-module-system/             # System module (RBAC core)
│   └── src/main/java/io/riverguard/
│       └── module/system/
│           ├── config/                   # Security, MyBatis-Plus, JWT config
│           ├── controller/               # Auth, User, Role controllers
│           ├── dto/                      # LoginDTO, UserCreateDTO, etc.
│           ├── entity/                   # SysUser, SysRole, SysUserRole, SysRoleScope
│           ├── mapper/                   # MyBatis-Plus mappers
│           ├── security/                 # JwtTokenProvider, JwtAuthFilter, UserDetailsServiceImpl
│           ├── service/                  # Business logic (UserServiceImpl, etc.)
│           └── vo/                       # LoginVO, UserVO, RoleVO
├── riverguard-server/                    # Application entry point
│   └── src/main/
│       ├── java/io/riverguard/server/    # RiverGuardApplication
│       └── resources/                    # application.yml, application-dev.yml
└── docs/sql/                             # Database migration scripts
    └── V1__init.sql                      # Initial schema + seed data
```

## Getting Started — 快速开始

### Prerequisites — 前置条件

- JDK 21+
- Maven 3.9+
- PostgreSQL 16+

### 1. Database Setup — 数据库初始化

```bash
# Create database
createdb riverguard

# Run initialization script
psql -d riverguard -f docs/sql/V1__init.sql
```

### 2. Configuration — 配置

The repository ignores sensitive `application.yml` and `application-dev.yml` files. Use the example templates:

```bash
cp riverguard-server/src/main/resources/application.example.yml riverguard-server/src/main/resources/application.yml
cp riverguard-server/src/main/resources/application-dev.example.yml riverguard-server/src/main/resources/application-dev.yml
```

Edit the copied files with your database credentials and JWT secret.

#### Default Credentials — 默认账号

After running `V1__init.sql`, an admin account is created:

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin123` | SUPER_ADMIN |

**Change the default password immediately in production.**

### 3. Build & Run — 构建与运行

```bash
# Compile (skip tests if no database)
mvn clean compile

# Package
mvn clean package -DskipTests

# Start server
mvn spring-boot:run -pl riverguard-server
```

The server starts on `http://localhost:8080`.

### 4. Verify — 验证

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

## API Overview — API 概览

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/login` | User login | No |
| POST | `/api/auth/logout` | User logout | Yes |
| GET | `/api/auth/me` | Current user info | Yes |
| GET | `/api/users` | Paginated user list | ADMIN+ |
| POST | `/api/users` | Create user | ADMIN+ |
| PUT | `/api/users/{id}` | Update user | ADMIN+ |
| DELETE | `/api/users/{id}` | Delete user | SUPER_ADMIN |
| GET | `/api/users/{id}/roles` | Get user roles | ADMIN+ |
| PUT | `/api/users/{id}/roles` | Assign user roles | ADMIN+ |
| GET | `/api/roles/scopes` | List manageable roles | Yes |

## Security — 安全

- **BCrypt**: All passwords hashed with BCrypt
- **JWT**: Token-based authentication with jti blacklist on logout
- **@PreAuthorize**: Method-level security with RBAC annotations
- **Stateless**: No HTTP session — pure JWT
- **Audit**: All API requests are logged

---

## Development — 开发

### Coding Conventions — 编码规范

- Lombok annotations on all POJOs and DTOs
- `@Valid` on all `@RequestBody` parameters
- Constructor injection (no `@Autowired` on fields)
- Parameterized SLF4J logging (no string concatenation)
- N+1 query prevention via batch collection loading

### Adding a New Module — 新增模块

```bash
# 1. Create Maven submodule
# 2. Add dependency in riverguard-server/pom.xml
# 3. Implement domain entity, mapper, service, controller
# 4. Register security rules in SecurityConfig
```

---

**RiverGuard AI Server** — Backend service for the RiverGuard AI Platform.
