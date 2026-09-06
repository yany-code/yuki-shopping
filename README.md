# Yuki Shopping 后端骨架

基于 Java 17、Spring Boot 3.3、MyBatis-Plus、MySQL 8 的商城后端起始工程。

## 启动

1. 执行 `docs/01_schema.sql` 初始化数据库，再执行 `docs/02_seed.sql` 写入演示数据（管理员/用户/分类/品牌/商品）。
2. 复制 `.env.example` 为 `.env` 并填入密码和 JWT 密钥（`cp .env.example .env`，密钥可用 `openssl rand -hex 32` 生成）。docker-compose 与 Spring 应用共用该文件；生产环境不部署 `.env`，直接注入同名环境变量即可。
3. 安装 Maven 后执行 `mvn spring-boot:run`，或导入 IntelliJ IDEA 后运行 `YukiShoppingApplication`。

当前骨架按 `modules/{user,product,cart,order,admin}` 划分，已提供统一响应、分页对象、参数校验、全局异常处理、Spring Security 访问边界，以及接口文档中主要客户端和管理端路由占位。控制器中的 `TODO` 是后续接入 entity、mapper、service 和 JWT 登录实现的明确扩展点；数据库事务、库存乐观锁和支付验签必须在 service 层实现，不能放在控制器中。

## 目录约定

- `common/api`：统一响应和分页对象
- `common/exception`：业务异常
- `config`：安全与基础设施配置
- `modules/*/controller`：按业务域组织接口
- `modules/*/service`：事务编排、状态机和权限校验（下一步实现）
- `modules/*/repository`：MyBatis-Plus Mapper、XML 和实体（下一步实现）

接口完整清单见 [`docs/api接口库.md`](docs/api接口库.md)，机器可读定义见 [`openapi.yaml`](openapi.yaml)。
