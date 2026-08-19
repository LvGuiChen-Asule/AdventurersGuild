# 14 Lore / 档案系统

## WHY：为什么需要 Lore？

Lore 不是"讲故事"，而是给探索和成就**发证书**：

- 玩家做了值得纪念的事（发现公会、进下界、杀龙），档案里就多一页；
- 让"完成 100 次委托"这种数字成就拥有情感落点（最后一页）；
- 12 条档案形成一条可拼图的世界观线索，为 Endgame 埋钩子；
- 档案是**可收集目标**：终局成就"未知地图（5 份）/ 旧时代（12 份）"把它们变成长期目标。

## WHAT：12 条 Lore（V1.1 已实现）

| ID | 名称 | 解锁事件 | 世界观作用 |
| --- | --- | --- | --- |
| LORE_001 | 第一份公会记录 | EVENT_GUILD_FOUND | 公会原点："让世界知道我们来过" |
| LORE_002 | 旧时代冒险者 | EVENT_GUILD_REGISTER | 职业传承：冒险者的代价 |
| LORE_003 | 关于下界 | EVENT_FIRST_NETHER | 重新定义下界：另一张地图 |
| LORE_004 | 未完成的地图 | EVENT_FIRST_ENDER_EYE | 埋主线钩子：要塞之后 |
| LORE_005 | 第一任会长 | EVENT_NETHER_FORTRESS | 角色侧写："门在后面" |
| LORE_006 | 龙 | EVENT_FIRST_END | 龙不是怪物，是守卫 |
| LORE_007 | 终末 | EVENT_DRAGON_DEATH | 终末不是终点（Endgame 宣言） |
| LORE_008 | 空白档案 | EVENT_STRONGHOLD_FOUND | 空白档案属于"未来的某个人"（玩家） |
| LORE_009 | 凋灵 | EVENT_WITHER_SUMMON | 存在主义提问："因为它被召唤了" |
| LORE_010 | 破碎的末地地图 | EVENT_END_ISLAND | 外岛指向"更远" |
| LORE_011 | 会长手记 | 事件联动（伊莱恩对话） | 记录冒险而非记录胜利 |
| LORE_012 | 最后一页 | EVENT_100_QUESTS | 全篇落点："冒险永不结束" |

> 12 条全部走"事件自动发现"机制；LORE_011 与伊莱恩的口述 Lore 联动。

## HOW：玩家怎么获得与查看？

1. 触发对应世界事件 → `ChronicleManager.recordEvent` 自动发现关联档案；
2. 聊天栏提示"发现档案：xxx"；
3. 打开冒险者档案（ChronicleScreen，快捷键 K 或找伊莱恩）查看已发现的事件与档案；
4. 与伊莱恩对话选择"讲讲公会的历史"获得口述 Lore；
5. 终局成就"未知地图（5 份）/ 旧时代（12 份）"把收集变成长期目标。

## DATA：Lore 数据结构（V1.1 已实现）

```json
{
  "id": "LORE_001",
  "title": "lore.adventurersguild.LORE_001.title",
  "text": "lore.adventurersguild.LORE_001.text",
  "unlock_event": "EVENT_GUILD_FOUND"
}
```

玩家发现状态存 `ChronicleState.loreDiscovered`（NBT）；世界级全局档案存 `GuildWorldData.globalLore`（SavedData）。

## IMPLEMENT：怎么落地？

- `data/LoreRegistry`：JSON 加载 + 按事件反查；
- `chronicle/ChronicleManager`：记录事件时联动发现档案；
- `player/ChronicleState`：玩家档案集合；
- `client/screen/ChronicleScreen`：事件 + 档案 + 拜访计数分块展示；
- 成就任务（ACHIEVEMENT）：`target=loreCount, amount=5/12` 从档案集合计数。

## VALUE：体现什么策划能力？

- **世界观构建**：12 条档案拼出完整母题——"记录冒险、冒险永不结束"，且每条都挂具体行为；
- **探索奖励设计**：Lore 作为行为成就的"证书"，成本低、收集感强；
- **Endgame 钩子**：空白档案（LORE_008）与最后一页（LORE_012）都指向"玩家自己续写"；
- **数据驱动**：Lore 与事件解耦，加一条 Lore 只需一个 JSON + 一次关联，不动代码。

## V1.2 规划（未实现，仅规划）

- V1.2 计划：Lore 在建筑内的实体书/书架散落收集（物理发现）、章节完成时展示书页动画。
