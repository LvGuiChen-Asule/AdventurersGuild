# Adventurer's Guild — 任务系统设计（V0.1 → V1.0）

## 1. 任务定义（JSON，数据驱动）

文件位置：`src/main/resources/data/adventurersguild/quests/*.json`（文件名必须全小写）。

```json
{
  "id": "collect_iron_ingots",
  "type": "COLLECT",
  "quality": "COMMON",
  "title": "quest.adventurersguild.collect_iron_ingots.title",
  "description": "quest.adventurersguild.collect_iron_ingots.desc",
  "objective": { "target": "minecraft:iron_ingot", "amount": 16 },
  "reward": { "gold": 50, "exp": 30, "reputation": 10 },
  "recommended_level": 1,
  "min_level": 1,
  "min_reputation": 0,
  "time_limit_seconds": 2700,
  "tutorial": false
}
```

字段说明：

| 字段 | 说明 |
| --- | --- |
| `id` | 任务唯一 ID（命令/接取使用的 quest_id） |
| `type` | COLLECT / HUNT / EXPLORE / SURVIVE / TRANSPORT / ELITE |
| `quality` | COMMON(1.0) / UNCOMMON(1.25) / RARE(2.0) / EPIC(3.0) / LEGENDARY(5.0) |
| `title` / `description` | 语言键 |
| `objective.target` | 物品 ID、`#标签`、实体 ID、生物群系 ID 或维度 ID |
| `objective.amount` | 数量（EXPLORE 可不填；SURVIVE 为秒数） |
| `objective.extra` | SURVIVE 条件（any/night/biome/dimension）；TRANSPORT 目标（spawn 或 "x,z"） |
| `objective.radius` | TRANSPORT 送达半径 |
| `reward.gold/exp/reputation` | 奖励（实际值 = 基础值 × 品质倍率 × 装备加成） |
| `min_level` / `min_reputation` | 接取门槛（V0.2 起生效） |
| `time_limit_seconds` | 超时自动放弃 |
| `tutorial` | 新手任务标记（置顶、一次性） |

## 2. 任务类型语义

| 类型 | 判定方式（全部服务端） |
| --- | --- |
| COLLECT | 拾取物品事件匹配物品 ID / 标签 |
| HUNT | 击杀事件匹配实体类型 |
| EXPLORE | 每秒检查玩家所在生物群系 == target |
| SURVIVE | 每秒 +1（条件满足且存活），死亡重置为 0 |
| TRANSPORT | 玩家处于送达区域时从背包扣除物品 |
| ELITE | 接受任务后对应实体生成时被强化（+150% 生命、精英前缀），击杀带 `ag_elite` 标记的实体计数 |

## 3. 任务状态机

```
            接取                首次进度
AVAILABLE ────────► ACCEPTED ──────────► IN_PROGRESS ──达标──► COMPLETED（发奖后移除）
                      │                     │
                      │ 放弃/超时            │ 放弃/超时
                      ▼                     ▼
                  ABANDONED             ABANDONED
```

- 上限 3 个进行中任务；同一任务不可重复接取；已完成的任务（每日板任务）之后日期可再次接取
- 任务完成即从进行中移除，奖励只发一次（防重复奖励）

## 4. 每日任务板（V0.4）

- 数据源：`quest_pools/daily.json`（quest_id / weight / min_level / max_level）
- 每天生成：普通 ×3 + 优秀 ×2 + 稀有 ×1（按品质槽位加权抽取）
- 每日免费刷新 3 次，之后 50 / 100 / 150 / 200 金币（封顶 200）
- 任务板存于世界 SavedData，重启后同一天保持同一批任务
- 大厅只显示：新手任务 + 今日任务板 + 已解锁任务链步骤（去重）

## 5. 任务链（V0.8）

- 数据源：`quest_chains/*.json`（chain_id / title / steps[{step, quest_id, unlock{type,value}}]）
- 第 0 步解锁条件：`level` 或 `reputation`；后续步骤由链进度（前置任务完成）解锁
- 完成任务时若属于某链的当前步骤，自动推进链进度并提示

## 6. V1.0 任务清单（32 个）

| 类型 | 数量 | 示例 |
| --- | --- | --- |
| 新手 | 1 | Tutorial_001 收集 10 原木（20G/20EXP/10声望） |
| 采集 | 9 | 木材储备、铁矿储备、煤矿补给、原矿收购、麦田供应、骨粉订单、火药备料、钻石委托、末影珍珠 |
| 狩猎 | 8 | 夜间清剿、蛛网除害、骷髅射手、黏液清理、爆破隐患、末影骚扰、女巫悬赏、烈焰猎杀 |
| 探索 | 5 | 平原勘察、森林测绘、沙漠远征、丛林探秘、雪原侦察 |
| 生存 | 3 | 守夜人、沙漠求生、下界试炼 |
| 运输 | 3 | 木材/铁矿/黄金押运（送至世界出生点） |
| 精英 | 3 | 精英僵尸/蜘蛛/骷髅讨伐 |

## 7. 奖励与成长

- 奖励 = 基础值 × 品质倍率 ×（1 + 饰品加成：冒险者徽章 +5% EXP、公会徽章 +5% 金币）
- 等级曲线：Lv1=0 / Lv2=300 / Lv3=900 / Lv4=2000 / Lv5=4000 / Lv6=7500（总 EXP）
- 声望：0 陌生人 / 100 新人 / 300 可靠冒险者 / 800 公会成员 / 1500 精英冒险者 / 3000 公会名人
