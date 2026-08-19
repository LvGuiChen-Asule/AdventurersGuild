# Adventurer's Guild — 系统架构（V0.1 → V1.0）

## 1. 设计原则

| 原则 | 落地方式 |
| --- | --- |
| MVP 优先 | 先保证"注册→接任务→执行→完成→奖励→保存"闭环，再逐版本扩展 |
| 数据驱动 | 任务/任务池/任务链/商店/装备全部来自 `data/adventurersguild/*/*.json` |
| Server Authority | 接受/进度/完成/发奖/购买/刷新全部服务端校验，客户端只发请求与显示快照 |
| 可扩展 | 品质、类型、状态均为可枚举/可解析数据；版本演进只加数据与少量处理逻辑 |
| 不过度工程化 | 无数据库、无 HTTP、无依赖注入框架，全部使用 Forge 原生能力 |
| 软依赖 | Curios 通过反射可选读取；不存在时核心系统照常运行（手持演示效果） |

## 2. 包结构

```
com.adventurersguild
├── AdventurersGuild.java     入口（事件/数据包/命令/注册）
├── quest/                    Quest / QuestType / QuestQuality / QuestStatus /
│                             QuestProgress / QuestPoolEntry / QuestChain /
│                             QuestManager（全部服务端权威逻辑）
├── player/                   AdventurerData（能力+NBT）/ AdventurerCapability /
│                             LevelData / ReputationData
├── data/                     QuestRegistry / QuestPoolRegistry / QuestChainRegistry /
│                             ShopRegistry / EquipmentRegistry / GuildDataLoader /
│                             DailyBoardManager（世界 SavedData）
├── economy/                  Shop / ShopItem
├── equipment/                EquipmentData / EquipmentEffects（Curios 软集成）
├── npc/                      GuildNpcHandler（标记村民 NPC）
├── registry/                 AGRegistry（物品/方块/创造标签）/ GuildTerminalBlock
├── network/                  6 个数据包 + SimpleChannel
├── command/                  AGCommands
└── client/                   6 个界面 + 客户端数据持有与包处理
```

## 3. 数据流

### 接任务（含门槛校验）

```
QuestBoardScreen [接受] → AcceptQuestPacket
  → QuestManager.acceptQuest
  → 校验：注册 / 任务存在 / 未重复 / 未满 3 个 / 等级 / 声望 / 任务板或链解锁
  → AdventurerData.acceptQuest(quest, gameTime)
  → GuildDataPacket.update 刷新客户端
```

### 进度（按类型）

- COLLECT：`PlayerEvent.ItemPickupEvent` 匹配物品/标签
- HUNT：`LivingDeathEvent` 匹配实体类型
- ELITE：`MobSpawnEvent.FinalizeSpawn` 强化精英 + 死亡时按 `ag_elite` 标记计数
- EXPLORE / SURVIVE / TRANSPORT：`TickEvent.PlayerTickEvent` 每秒判定一次

### 奖励（防重复）

```
达标 → 状态置 COMPLETED → 从进行中移除 → 计数 → 发金币/EXP/声望
    → 品质倍率 × 装备加成 → 等级提升提示 → 任务链推进 → 同步客户端
```

## 4. 持久化

- 玩家数据：`AdventurerData` 能力 NBT（registered/level/experience/gold/reputation/
  activeQuests/completedQuestCount/completedQuestIds/abandonedQuestCount/
  dailyRefreshCount/lastRefreshDay/chainProgress），随存档保存，死亡克隆保留
- 每日任务板：`GuildDailyData`（SavedData），按世界天数缓存当日任务

## 5. 防作弊与正确性

- 客户端无法直接修改任何任务/金币状态
- 非法任务 ID / 未上板任务 / 等级声望不足 → 服务端拒绝并提示
- 负数/超大金币被拒绝，金币上限 1,000,000,000
- 重复奖励：完成即移除任务再发奖，`COMPLETED` 状态守卫
- 商店购买：等级门槛 + 金币校验 + 背包满时掉落，非法商品退款

## 6. 性能约束（V0.9）

- 任务进度只在事件发生时处理；EXPLORE/SURVIVE/TRANSPORT/超时每秒检查一次
- 无逐 tick 实体/背包扫描；精英生成只在生成事件时检查
- 数据同步只发给数据变化的玩家，不广播

## 7. 网络协议

通道 `adventurersguild:main`，协议版本 `1`：

| ID | 包 | 方向 | 用途 |
| --- | --- | --- | --- |
| 0 | GuildDataPacket | S2C | 数据快照 + 打开/刷新界面 |
| 1 | AcceptQuestPacket | C2S | 请求接任务 |
| 2 | AbandonQuestPacket | C2S | 请求放弃任务 |
| 3 | RegisterAdventurerPacket | C2S | 请求注册 |
| 4 | BuyItemPacket | C2S | 商店购买 |
| 5 | RefreshBoardPacket | C2S | 刷新任务板 |

## 8. 世界入口（V0.7）

- 公会终端方块：右键打开任务大厅（创造标签可获取）
- `/ag spawnnpc <role>`：生成接待员 / 任务管理员 / 商店管理员（标记村民）
- NPC 与终端功能并存；无 NPC / 无终端时命令仍可进入所有界面
