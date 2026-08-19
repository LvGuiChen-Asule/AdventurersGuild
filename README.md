# Adventurer's Guild（冒险者公会）V1.1

Minecraft **1.20.1 Forge** 轻量 RPG 冒险框架 Mod（V1.1）。

> 本项目主要用于展示游戏系统策划、任务设计、数值设计、UI/UX 设计与 Minecraft Mod 工程实现能力。

## 项目简介

V1.1 定位为**基于 Minecraft 原版生存流程的轻量 RPG 冒险框架**：

发现公会 → 注册冒险者 → 接取支线 → 推进原版冒险（主世界 → 下界 → 末地）→ 世界事件 → NPC 对玩家行为产生反应 → 击败末影龙 → Endgame。

主线是**弱线性**的：玩家可以提前进入下界/末地、提前击杀末影龙，系统**记录已完成的行为**，绝不阻止玩家玩原版。

## 功能总览

| 系统 | 内容 |
| --- | --- |
| 任务系统 | 80 个数据驱动任务，9 种类型（COLLECT/HUNT/EXPLORE/SURVIVE/TRANSPORT/ELITE/INTERACT/MILESTONE/ACHIEVEMENT） |
| 主线 | 5 章（序章/踏上旅途/烈焰之地/终末之地/Endgame），24 主线 + 24 终局任务，事件驱动解锁 |
| 世界事件 | 15 个事件，once=true，提前完成自动记录 |
| NPC | 6 名核心 NPC（真实实体 + 日程 AI + 数据驱动对话 + 事件响应） |
| 公会建筑 | guild_hall.nbt（45×20×35）地表确定性生成，含地形清理、补门、4 格空档 |
| 冒险者档案 | Chronicle + 12 条 Lore |
| 冒险团 | Party（创建/邀请/加入/离开/解散） |
| 经济/装备/成长 | V1.0 全量保留（每日任务板/品质/金币/EXP/声望/商店/饰品/Curios 可选） |
| UI | 深木/羊皮纸/暗金主题 10 个页面 |
| 快捷键 | G/J/K/L（可重绑） |
| 持久化 | 玩家能力 NBT + GuildWorldData + 实体持久化；服务器权威 |

## 配表

[docs/portfolio/assets/Economy.png](docs/portfolio/assets/Economy.png)
[docs/portfolio/assets/Equipment.png](docs/portfolio/assets/Equipment.png)
[docs/portfolio/assets/Level.png](docs/portfolio/assets/Level.png)
[docs/portfolio/assets/Quest.png](docs/portfolio/assets/Quest.png)
[docs/portfolio/assets/QuestChain.png](docs/portfolio/assets/QuestChain.png)
[docs/portfolio/assets/QuestPool.png](docs/portfolio/assets/QuestPool.png)
[docs/portfolio/assets/Reputation.png](docs/portfolio/assets/Reputation.png)
[docs/portfolio/assets/Shop.png](docs/portfolio/assets/Shop.png)
[docs/portfolio/assets/TestData.png](docs/portfolio/assets/TestData.png])

## 技术栈

Minecraft 1.20.1 · Forge 47.4.22 · Java 17 · Gradle 8.8 · ForgeGradle 6.0.54 · Mod ID `adventurersguild` · Package `com.adventurersguild`

**前置 Mod**：无。Curios 为可选（无 Curios 时饰品手持生效）。

## 系统架构

```
Client(显示/请求) → C2S 包 → 服务端系统（Quest/Chapter/Chronicle/Dialogue/Party/Event）
                                      ↓
                              AdventurerData(NBT) / GuildWorldData(SavedData)
                                      ↓
                  S2C 包（GuildDataSync/Chronicle/Dialogue/Party/Notification）
```

详见 [docs/architecture.md](docs/architecture.md)、[docs/quest_system.md](docs/quest_system.md)、[docs/v1.1_baseline.md](docs/v1.1_baseline.md)。

## GitHub

公开仓库：[LvGuiChen-Asule/AdventurersGuild](https://github.com/LvGuiChen-Asule/AdventurersGuild)

## 作品集

《Adventurer's Guild 游戏系统策划集》（29 章，基于真实代码与实测数据）：

[docs/portfolio/README.md](docs/portfolio/README.md)

## 安装与开发

1. 安装 MC 1.20.1 + Forge 47.4.22；将 `build/libs/adventurersguild-1.1.0.jar` 放入 `mods/`。
2. `gradlew build` 构建；`gradlew runClient` / `gradlew runServer` 开发运行。

## 指令

- 通用：`/ag register` `info` `quests` `myquests` `adventurer` `shop` `chains` `chronicle` `guild` `guild locate` `refresh` `abandon`
- 冒险团：`/ag party create|invite|join|accept|leave|disband|info`
- 开发（OP2）：`/ag givegold` `addexp` `setrep` `complete` `reset` `spawnnpc` `debug chapter|event|npc|guild|reset`

## Demo

10~12 分钟作品集演示流程见 [docs/v1.1_demo.md](docs/v1.1_demo.md)；完整 Demo 脚本见 [docs/portfolio/25_demo.md](docs/portfolio/25_demo.md)。

## 开发进度

- [√] V0.1–V1.0（任务/成长/声望/经济/装备/NPC/公会/任务链/UI/持久化）
- [√] V1.1：能力重构、GuildWorldData、公会建筑与自然生成、6 NPC 实体与 AI、对话框架、Chronicle、15 世界事件、章节主线、Lore、Endgame、冒险团、UI 主题与快捷键、网络重构、Excel 21 表、服务器冒烟测试
- [ ] 真实游玩回归（TestData 待验证项）、多人测试、截图与视频

## 开源协议

All Rights Reserved（作品集演示项目）。
