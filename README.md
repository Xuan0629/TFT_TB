# 🧩 TFT Team Builder Backend (S15)

基于 Java Spring Boot 的云顶之弈团队构建工具后端服务。  
当前版本实现了 S15 数据导入、英雄/羁绊/职业建模、JWT 用户系统、以及阵容评分算法的初步实现。

本项目是一个「可继续扩展」的 TFT 数据服务，用于阵容推荐、羁绊模拟器、评分系统、或前端 TFT 阵容构建器。

---

## ⭐ 核心功能

### ✔ 数据导入与解析 (CDragon S15)
- 解析官方 CDragon 静态 JSON（中文 zh_cn）
- 自动导入 S15 的英雄、羁绊、职业、技能描述
- 具备幂等性：重复启动不会重复插入
- 自动建立关系：
  - Champion ↔ Traits (多对多)
  - Champion → Role (多对一)
  - Champion → Ability (多对一)

### ✔ 完整用户系统 (JWT Auth)
- 用户注册、登录
- 基于 JWT 的无状态鉴权
- Spring Security 6 适配配置
- 支持 Bearer Token 访问 API

### ✔ 阵容评分算法（初版雏形）
- 根据羁绊等级、单位 cost、职业搭配初步评分  
- 使用 Trait levels 信息进行「羁绊激活评分」
- 将 Role、Ability 作为未来算法扩展的基础构件

### ✔ REST API
- `/api/champions`
- `/api/traits`
- `/api/auth/**`
- `/api/health`

---

## 🛠 技术栈

| 部分 | 技术 |
|------|------|
| 编程语言 | Java 21 |
| 框架 | Spring Boot 3 |
| 数据库 | MySQL 8 |
| 持久化 | Spring Data JPA |
| 安全 | Spring Security 6 + JWT |
| JSON | Jackson |
| 工具 | Maven、Lombok、HikariCP |

---

## 📦 项目结构

```
tft_backend/
├── config/
│   ├── SecurityConfig.java
│   └── StartupImporter.java
│
├── controller/
│   ├── AuthController.java
│   ├── ChampionController.java
│   ├── TraitController.java
│   └── ScoringController.java
│
├── entity/
│   ├── Champion.java
│   ├── Trait.java
│   ├── Role.java
│   ├── Ability.java
│   └── User.java
│
├── filter/
│   └── JwtAuthenticationFilter.java
│
├── repository/
│   ├── ChampionRepository.java
│   ├── TraitRepository.java
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   └── AbilityRepository.java
│
├── service/
│   ├── JwtService.java
│   ├── UserService.java
│   ├── ScoringService.java
│   └── importer/
│       ├── CdragonParser.java
│       └── DataImportService.java
│
└── TftBackendApplication.java
```

---

## 🧩 数据模型

### Champion
- `name`
- `cost`
- `traits` (ManyToMany)
- `role` (ManyToOne)
- `ability` (ManyToOne)

### Trait
- `name`
- `setName` (S15)
- `levels: List<Integer>`
- 与 Champion 多对多关联

### Role（新增）
如：
```
ADTank
APTank
ADCaster
APCaster
Bruiser
Enchanter
```

### Ability（新增）
如：
```
IncreaseTeamAtkSpeed
ReduceEnemyArmor
ReduceEnemyMR
IncreaseAD
```

---

## 📥 数据导入（CDragon）

将 S15 JSON 放在：
```
src/main/resources/cdragon/zh_cn.json
```

启动应用时自动导入。

包括：
- 英雄
- 羁绊
- 职业（由内部规则分类）
- 技能（从 JSON 中 ability 结构解析）

---

## 🔐 JWT 认证

### 登录示例
```
POST /api/auth/login
{
  "username": "sean",
  "password": "123456"
}
```

返回：
```
{
  "token": "<JWT>"
}
```

后续请求需携带：
```
Authorization: Bearer <JWT>
```

---

## 📡 API 一览

### 健康检查
```
GET /api/health
```

### 用户系统
```
POST /api/auth/register
POST /api/auth/login
```

### 英雄
```
GET /api/champions
GET /api/champions/{id}
GET /api/champions/set/S15
```

### 羁绊
```
GET /api/traits
GET /api/traits/{id}
GET /api/traits/set/S15
```

### 阵容评分（初版）
```
POST /api/scoring/score
```

---

## 🧪 数据模板（用于未来手动新增）

### Champion JSON 模板
```json
{
  "name": "拉克丝",
  "cost": 2,
  "traits": ["斗魂战士", "法师"],
  "role": "APCaster",
  "ability": "减少敌方魔抗"
}
```

### Trait JSON 模板
```json
{
  "name": "裁决使者",
  "setName": "S15",
  "description": "获得暴击几率和暴击伤害。",
  "levels": [2, 3, 4, 5]
}
```

---

# 🧭 Roadmap (未来计划)

| 功能 | 状态 |
|------|------|
| S15 数据导入 | ✅ 已完成 |
| JWT 用户系统 | ✅ 已完成 |
| 职业与技能系统 | ✅ 已完成 |
| 阵容评分算法（V1） | ✅ 已完成 |
| 阵容评分算法（V2 深度评分如装备、站位） | ⏳ 规划中 |
| 阵容推荐器（协同过滤 / 规则推荐） | ⏳ 规划中 |
| S16 数据支持 | ⏳ 计划中 |
| 中英文双语言数据支持 | ⏳ 计划中 |
| 前端 UI（React 或 Vue） | ⏳ 计划中 |
| 使用 Redis 做缓存 | ⏳ 计划中 |
| OpenAPI / Swagger 文档 | ⏳ 计划中 |
| Docker 化 | ⏳ 可选 |
| 单元测试 80% 覆盖率 | ⏳ 待补充 |


---

# ⚠️ 当前限制与不足（Limitations）

### 1. **仅支持 S15**
- 导入器目前硬编码为 S15  
- 无法载入 S16 或更旧版本

### 2. **仅使用 zh_cn 数据**
- CDragon JSON 语言为中文  
- 英文名称、翻译需后续补全

### 3. **技能与职业分类为内部推断**
- Role 与 Ability 目前是开发者主观分类  
- 暂未基于 Riot 官方职业体系自动识别

### 4. **阵容评分算法为初版**
- 不考虑以下因素：
  - 装备
  - 站位
  - 打工过渡
  - 强势期曲线
  - 特殊机制（如强化符文）
- 主要用于验证项目架构

### 5. **无前端界面**
- 目前后端 API 只能通过 Postman 或 curl 调用

### 6. **无 Swagger 文档**
- API 未自动生成文档，需要在 IDE 内查看代码

---

# ▶ 运行方式

## 1. 配置 MySQL
```sql
CREATE DATABASE tft CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 2. application.properties
```
spring.datasource.url=jdbc:mysql://localhost:3306/tft
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

jwt.secret=YOUR_SECRET_KEY
jwt.expiration=86400000
```

## 3. 启动应用
```
mvn clean install
mvn spring-boot:run
```

---

# 🤝 贡献

欢迎 Issue 与 Pull Requests。

---

# 📄 License

MIT License
