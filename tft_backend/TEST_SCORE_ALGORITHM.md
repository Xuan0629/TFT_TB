# 阵容评分算法测试方案

## 概述

本文档提供了完整的测试方案，用于验证 `CompService.calculateScore()` 方法的正确性。

## 测试方法

### 1. 单元测试（自动）

运行以下命令执行单元测试：

```bash
cd tft_backend
./mvnw.cmd test -Dtest=CompServiceScoreTest
```

或者在 IDE 中直接运行 `CompServiceScoreTest` 类。

### 2. 集成测试（手动）

通过 API 端点进行实际测试：

#### 2.1 准备测试数据

确保数据库中已有以下数据：
- 多个不同 cost 的单位（1-5）
- 不同 role 的单位（ADCarry, APCarry, ADTank, APTank, ADFighter, APFighter 等）
- 不同 ability 的单位（包含"眩晕"、"友军"、"护甲削减"、"魔抗削减"、"重伤"等关键词）
- 不同羁绊（包括特殊羁绊：大宗师、兵王、决斗大师）

#### 2.2 测试场景

##### 场景1：基础单位得分测试

**请求：**
```bash
POST http://localhost:8080/api/comps/preview
Content-Type: application/json
Authorization: Bearer <token>

{
  "championIds": [1, 2, 3],
  "name": "基础测试",
  "description": "测试基础单位得分"
}
```

**预期：**
- cost=1 的单位贡献 10 分
- cost=2 的单位贡献 20 分
- cost=3 的单位贡献 30 分
- 总分 = 60 分（无羁绊时）

##### 场景2：Ability 加成测试 - 眩晕

**请求：**
```bash
POST http://localhost:8080/api/comps/preview
Content-Type: application/json
Authorization: Bearer <token>

{
  "championIds": [<包含"眩晕"的单位ID>],
  "name": "眩晕测试"
}
```

**预期：**
- 该单位得分 = cost × 10 × 1.5

##### 场景3：Ability 加成测试 - 护甲削减（全局）

**请求：**
```bash
POST http://localhost:8080/api/comps/preview
Content-Type: application/json
Authorization: Bearer <token>

{
  "championIds": [<包含"护甲削减"的单位ID>, <其他单位ID>],
  "name": "护甲削减测试"
}
```

**预期：**
- 总分 = (单位总分 + 羁绊总分) × 1.2

##### 场景4：羁绊得分测试

**请求：**
```bash
POST http://localhost:8080/api/comps/preview
Content-Type: application/json
Authorization: Bearer <token>

{
  "championIds": [<拥有相同羁绊的2个单位ID>],
  "name": "羁绊测试"
}
```

**预期：**
- 羁绊激活（达到最低 level）
- 基础分 = 10（最低 level）
- 加权 = 单位 cost 之和 × 0.1
- 羁绊得分 = 基础分 × (1 + 加权)

##### 场景5：特殊羁绊测试 - 大宗师

**请求：**
```bash
POST http://localhost:8080/api/comps/preview
Content-Type: application/json
Authorization: Bearer <token>

{
  "championIds": [<拥有"大宗师"羁绊的1个单位ID>],
  "name": "大宗师1单位测试"
}
```

**预期：**
- 羁绊得分 > 0（1 单位时生效）

```bash
POST http://localhost:8080/api/comps/preview
Content-Type: application/json
Authorization: Bearer <token>

{
  "championIds": [<拥有"大宗师"羁绊的2个单位ID>],
  "name": "大宗师2单位测试"
}
```

**预期：**
- 羁绊得分 = 0（2 单位时不生效）

##### 场景6：前后排平衡测试

**请求：**
```bash
POST http://localhost:8080/api/comps/preview
Content-Type: application/json
Authorization: Bearer <token>

{
  "championIds": [
    <1个Tank或Fighter单位ID>,
    <5个Carry或Caster单位ID>
  ],
  "name": "前后排不平衡测试"
}
```

**预期：**
- 总分 = (单位总分 + 羁绊总分) × 0.85（因为前排 < 后排 - 3）

##### 场景7：综合场景测试

**请求：**
```bash
POST http://localhost:8080/api/comps/preview
Content-Type: application/json
Authorization: Bearer <token>

{
  "championIds": [
    <包含"眩晕"的ADCarry单位>,
    <包含"护甲削减"的ADCarry单位>,
    <Tank单位>,
    <拥有level=1羁绊的单位>,
    <其他单位>
  ],
  "name": "综合测试"
}
```

**预期：**
- 所有加成正确应用
- 得分在合理范围内

## 测试检查清单

### 单位得分
- [ ] 基础分 = cost × 10
- [ ] "眩晕" ability → 单位得分 ×1.5
- [ ] "友军" ability → 单位得分 ×1.5
- [ ] level=1 羁绊 → 单位得分 ×1.5

### 羁绊得分
- [ ] 最低 level 基础分 = 10
- [ ] 最高 level 基础分 = maxLevel × 5
- [ ] 中间 level 线性插值
- [ ] 加权 = 所有单位 cost × 0.1 之和
- [ ] 最终得分 = 基础分 × (1 + 加权)

### 特殊羁绊
- [ ] 【大宗师】仅在 1 或 4 单位时生效
- [ ] 【兵王】最高层级额外 +10 分
- [ ] 【决斗大师】最高层级额外 +10 分
- [ ] level=1 羁绊不计入总分

### 全局加成
- [ ] "护甲削减/魔抗削减" → 总分 ×1.2（不叠加）
- [ ] "重伤" → 总分 ×1.2（不多次生效）

### 阵容类型
- [ ] AD 权重 ≥70% → 物理阵容
- [ ] AP 权重 ≥70% → 魔法阵容
- [ ] 其他 → 混伤阵容
- [ ] Tank 单位不计入权重计算

### 前后排平衡
- [ ] 前排 < 后排 - 3 → 总分 ×0.85
- [ ] Tank 和 Fighter 属于前排

## 调试技巧

### 1. 查看详细日志

在 `CompService.calculateScore()` 方法中添加日志输出：

```java
System.out.println("单位总分: " + totalChampionScore);
System.out.println("羁绊总分: " + totalTraitScore);
System.out.println("阵容类型倍数: " + compTypeMultiplier);
System.out.println("前后排倍数: " + frontBackMultiplier);
System.out.println("全局倍数: " + globalMultiplier);
System.out.println("最终得分: " + finalScore);
```

### 2. 使用 Postman 测试

1. 登录获取 JWT token
2. 使用 `POST /api/comps/preview` 测试不同阵容组合
3. 对比预期得分和实际得分

### 3. 数据库查询验证

```sql
-- 查看单位及其羁绊
SELECT c.name, c.cost, r.name as role, a.name as ability, t.name as trait
FROM champions c
LEFT JOIN roles r ON c.role_id = r.id
LEFT JOIN abilities a ON c.ability_id = a.id
LEFT JOIN champion_traits ct ON c.id = ct.champion_id
LEFT JOIN traits t ON ct.trait_id = t.id
WHERE c.set_name = 'S15'
ORDER BY c.cost, c.name;
```

## 常见问题

### Q1: 为什么某些羁绊得分为 0？

**A:** 可能原因：
- 羁绊未激活（单位数 < 最低 level）
- 【大宗师】在非 1/4 单位时
- 该羁绊被排除（因为某个单位有 level=1 的该羁绊）

### Q2: 为什么总分比预期低？

**A:** 检查：
- 前后排是否不平衡（触发 ×0.85 惩罚）
- 羁绊是否未激活
- 是否有特殊羁绊限制

### Q3: 如何验证加权计算？

**A:** 手动计算：
- 找出所有拥有该羁绊的单位
- 计算 cost 之和
- 加权 = cost 之和 × 0.1
- 验证：羁绊得分 = 基础分 × (1 + 加权)

## 测试数据建议

### 推荐测试单位组合

1. **纯物理阵容**：多个 ADCarry/ADCaster（无 Tank）
2. **纯魔法阵容**：多个 APCarry/APCaster（无 Tank）
3. **混伤阵容**：AD 和 AP 单位混合
4. **前后排不平衡**：1 个 Tank，5 个 Carry
5. **前后排平衡**：3 个 Tank/Fighter，3 个 Carry/Caster

### 推荐测试羁绊

1. **普通羁绊**：2/4/6 或 2/3/4 的羁绊
2. **特殊羁绊**：大宗师、兵王、决斗大师
3. **level=1 羁绊**：测试排除逻辑

## 性能测试

对于大量单位的阵容（8+ 单位），验证：
- 计算时间 < 100ms
- 内存使用正常
- 无死循环或栈溢出

## 总结

通过以上测试方案，可以全面验证评分算法的正确性。建议：
1. 先运行单元测试确保基础逻辑正确
2. 再通过 API 进行集成测试验证实际效果
3. 最后使用真实游戏数据进行验证

