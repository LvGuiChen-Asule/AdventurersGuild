# 开发日志（Development Log）

## 2026-08-16 — V1.1（TASK-001 → TASK-030）

- TASK-001：架构扫描与基线（44 Java/3915 行；baseline build PASS）
- TASK-002：AdventurerCapability 六状态重构（NBT 兼容）
- TASK-003：GuildWorldData（世界级 SavedData）
- TASK-004：guild_hall.nbt 自研 NBT 生成器（6301 方块）
- TASK-005：Jigsaw Structure 自然生成 + `/ag guild locate`
- TASK-006~009：GuildNPCEntity ×6 + 渲染 + 日程 AI
- TASK-010~012：对话框架 + Chronicle + 15 世界事件
- TASK-013~018：章节主线（INTERACT/MILESTONE/ACHIEVEMENT）+ Lore×12 + Endgame×24
- TASK-019：冒险团（Party）
- TASK-020~022：UI 主题 + 公会总览 + 装备页 + G/J/K/L 快捷键
- TASK-023：guild/chronicle/party/debug 指令
- TASK-024：网络协议 v2（GuildDataSync/Dialogue/Chronicle/Party/Notification）
- TASK-025：Excel 21 表配表
- TASK-026：runServer 冒烟测试 PASS（80 任务/22 池/3 链/3 店/5 装备/6 对话/5 章/12 Lore，Done 7.456s）
- TASK-027~030：回归/README/Demo/clean build → 1.1.0

### 关键修复
- 1.20.1 Wither 类为 `WitherBoss`；生物群系走 `registryAccess()`
- `ServerLevel.findNearestMapStructure`（非 StructureManager）
- 结构文件/任务文件名必须全小写；Windows 大小写文件冲突
- 批量重命名时 PowerShell 默认编码损坏非 ASCII 字符（已修复并加校验）

## 2026-08-15 — V0.1 核心任务 MVP

### 完成
- 数据驱动任务系统：QuestRegistry + 5 个 JSON 任务（采集 ×3、狩猎 ×2）
- 玩家数据能力（registered/level/experience/gold/activeQuests/completedQuestCount），NBT 持久化
- 服务端权威 QuestManager（注册/接取/进度/完成/放弃/超时/发奖）
- 4 个数据包、4 个界面（任务大厅/详情/我的任务/冒险者信息）、中英双语
- `/ag` 全套命令
- `gradle build` 成功；专用服务器启动，5 个任务加载成功（Done 17.454s）

### 关键修复
- `FriendlyByteBuf.Writer` 参数顺序 `(buf, value)`；实体 `getOptional` 返回 `EntityType<?>`
- 命令上下文需 `ctx.getSource()`

## 2026-08-15 — V0.2 ～ V0.4 成长、品质与随机任务

### 完成
- V0.2：Lv1–Lv6 等级曲线与称号、6 阶段公会声望、等级/声望接取门槛
- V0.3：6 个界面统一版式（品质色/状态色/进度条/滚动），冒险者信息页含 EXP/声望双进度条与成功率
- V0.4：5 品质倍率、任务池权重、每日任务板（普通×3/优秀×2/稀有×1）、免费 3 次+付费刷新（50/100/150/200）
- 新增 EXPLORE（生物群系判定）与 SURVIVE（夜晚/群系/维度条件，死亡重置）任务

## 2026-08-15 — V0.5 ～ V0.6 经济与装备

### 完成
- V0.5：ShopRegistry + 3 个商店（冒险补给 Lv1 / 高级补给 Lv3 / 特殊商品 Lv5），金币购买 + 等级解锁 + 背包溢出掉落
- V0.6：5 件饰品（冒险者徽章 +5% EXP / 矿工戒指 +5% 挖掘 / 猎人徽章 +5% 敌对伤害 / 探索护符 +5% 移速 / 公会徽章 +5% 金币）
- Curios 软集成（反射读取饰品槽），无 Curios 时手持兜底，核心系统不依赖

## 2026-08-15 — V0.7 ～ V0.8 NPC、公会与高级任务

### 完成
- V0.7：公会接待员 / 任务管理员 / 商店管理员（`/ag spawnnpc`）+ 公会终端方块 + 创造标签
- V0.8：3 条任务链（矿工之路/夜色猎手/公会试炼）、链前置解锁、精英怪强化（+150% 生命）、TRANSPORT 送达、ELITE 讨伐、高额奖励

## 2026-08-15 — V0.9 ～ V1.0 平衡、测试与正式版

### 完成
- 32 个任务（新手 1 / 采集 9 / 狩猎 8 / 探索 5 / 生存 3 / 运输 3 / 精英 3）
- 新手引导：Tutorial_001 置顶，完成后解锁正式任务大厅
- 性能约束：EXPLORE/SURVIVE/TRANSPORT/超时每秒检查，无逐实体扫描
- `clean build` 成功；服务器冒烟测试：32 任务 / 22 任务池条目 / 3 任务链 / 3 商店 / 5 装备全部加载（Done 7.435s）

### 关键修复
- 数据包文件名必须全小写（`Tutorial_001.json` → `tutorial_001.json`，资源路径不允许大写）
- Windows 大小写不敏感导致同名文件冲突：先清理再复制
- 1.20.1 生物群系注册表需通过 `registryAccess().registryOrThrow(Registries.BIOME)` 访问

### 遗留与下一步
- 真实游戏会话交互测试（注册→接取→各类型推进→完成→重启存档）待客户端实测
- 多人（2/5/10 人）串号测试、数值长期平衡测试
- 截图与 Demo 视频、Create 公会建筑整合
