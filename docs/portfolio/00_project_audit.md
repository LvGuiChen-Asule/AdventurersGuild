# 00 项目真实功能基线审计（PORTFOLIO-001）

> 依据：代码、资源、配表、构建结果。不根据任何 Prompt 或 README 猜测。

## 1. 项目基本信息（已验证）

| 项 | 值 | 依据 |
| --- | --- | --- |
| Minecraft / Forge | 1.20.1 / 47.4.22 | build.gradle、mods.toml |
| Java / Gradle | 17 / 8.8（ForgeGradle 6.0.54） | build.gradle |
| Mod ID / Package | `adventurersguild` / `com.adventurersguild` | mods.toml、源码 |
| 代码规模 | 86 个 Java 文件，约 7176 行 | 源码统计 |
| 构建 | `gradlew build` = PASS | 本次执行 |
| 版本控制 | 无 git 仓库（技术债） | `git status` |

## 2. 当前实际有什么功能？（代码/资源为准）

### 任务系统（V1.0 已实现）
- 80 个数据驱动任务（JSON），9 种类型：
  COLLECT×15 / HUNT×8 / EXPLORE×10 / SURVIVE×5 / TRANSPORT×3 / ELITE×3 / INTERACT×9 / MILESTONE×13 / ACHIEVEMENT×14
- 品质 5 级：COMMON×11 / UNCOMMON×7 / RARE×30 / EPIC×17 / LEGENDARY×15（倍率 1.0/1.25/2.0/3.0/5.0）
- 每日任务板：每天 3 普通 + 2 优秀 + 1 稀有（任务池 22 条加权条目）
- 刷新：每日免费 3 次，之后 50/100/150/200 Gold
- 状态机：AVAILABLE→ACCEPTED→IN_PROGRESS→COMPLETED/ABANDONED；最多 3 个进行中
- 超时自动放弃；防重复奖励（完成即移除再发奖）

### 成长与声望（V1.0 已实现）
- Lv1–Lv6，EXP 曲线 0/300/900/2000/4000/7500，6 个等级称号
- 声望 6 阶段：0/100/300/800/1500/3000
- 任务接取门槛：等级 / 声望 / 章节 / 前置

### 经济（V1.0 已实现）
- 3 商店：冒险补给（Lv1，4 件）、高级补给（Lv3，5 件）、特殊商品（Lv5，5 件）
- 金币购买 + 等级解锁 + 背包溢出掉落

### 装备（V1.0 已实现）
- 5 件饰品：冒险者徽章（任务 EXP+5%）、矿工戒指（挖掘+5%）、猎人徽章（敌对伤害+5%）、探索护符（移速+5%）、公会徽章（金币+5%）
- Curios 可选（反射软集成），无 Curios 时手持生效（演示兜底）

### NPC 与对话（V1.1 开发中，功能已实现）
- 6 个真实实体 NPC：艾琳（接待）/ 罗德（任务）/ 米娅（商店）/ 格雷（远征）/ 伊莱恩（档案）/ 塞拉斯（终末）
- 数据驱动对话：6 个 Dialogue JSON，条件与动作全部服务端判定
- NPC 日程 AI（工作/休息/自由/归家）+ 远离怪物 + 注视玩家

### 世界与主线（V1.1 开发中，功能已实现）
- 公会大厅：程序生成模板（45×20×35），地表确定性放置 + 地形清理 + 4 格空档
- 章节 5 个：序章 / 踏上旅途 / 烈焰之地 / 终末之地 / Endgame（事件驱动弱线性解锁）
- 主线任务 24 个（AG_MAIN_*）、Endgame 24 个（AG_END_*）
- 任务链 3 条（矿工之路 / 夜色猎手 / 公会试炼）
- 世界事件 15 个（once=true）；Lore 12 条（事件自动发现）

### 冒险团（V1.1 开发中，功能已实现）
- 创建 / 邀请 / 加入 / 退出 / 解散（团长制，轻量）

### UI（V1.1 开发中，功能已实现）
- 10 个界面：公会总览 / 任务大厅 / 任务详情 / 我的任务 / 冒险者 / 商店 / 任务链 / 档案 / 冒险团 / 对话
- 主题：深木 / 羊皮纸 / 暗金 / 深蓝；快捷键 G/J/K/L（可重绑）

### 技术与数据（V1.0/V1.1）
- 网络协议 v2：GuildDataSync + 5 个 C2S + 4 个 S2C（Dialogue/Chronicle/Party/Notification）
- 持久化：玩家能力 NBT（含 chronicle/lore/counters/unlocks/partyId）+ GuildWorldData（SavedData）+ 实体持久化
- 配表：AdventurersGuild_Data.xlsx（21 工作表）

## 3. 哪些功能真正实现 vs 只是宣称？

| 系统 | 状态 | 证据 |
| --- | --- | --- |
| 任务/品质/每日板/成长/声望/商店/装备/任务链/新手引导 | **已实现**（V1.0） | 代码 + JSON + 服务器加载日志 |
| NPC 实体/对话/事件/章节/Lore/Endgame/冒险团/档案/快捷键 | **已实现**（V1.1 功能完成） | 代码 + JSON + 服务器加载日志 |
| 公会建筑生成 | **已实现**（经多轮调试修复） | NBT 模板 + 放置代码 + 实测 |
| 真实游戏内完整验收 | **部分完成，验证进行中** | 用户实测中；部分项标"已实现待验证" |
| Curios 真实环境 | **未验证** | 反射路径未在装有 Curios 的环境测试 |
| 多人（2/5/10 人） | **未验证** | 设计为按玩家隔离，未实测 |

## 4. 当前 UI（10 个）

GuildMain / QuestBoard / QuestDetail / MyQuest / Adventurer / Shop / QuestChain / Chronicle / Party / Dialogue。

## 5. 当前任务（80 个，见上）

## 6. 当前 NPC（6 个，见上）

## 7. 当前装备（5 件饰品，见上）

## 8. 当前数据表（Excel 21 表）

Quest / QuestPool / QuestChain / Level / Reputation / Equipment / Shop / Economy / TestData / QuestReward / QuestCondition / Chapter / Milestone / NPC / WorldEvent / Lore / Party / Unlock / Balance / UI / Achievement。

## 9. 作品集亮点

1. 数据驱动全流程：JSON→Excel→运行时不依赖 Excel
2. 服务器权威：客户端只发请求，所有状态/奖励服务端判定
3. 弱线性主线：记录行为而非限制行为（15 个世界事件 once=true）
4. 世界反馈：NPC 对话随事件/章节变化
5. 完整 RPG 循环：任务→成长→声望→经济→Endgame
6. 真实落地：从 0 搭工程、调世界生成、修 NBT 格式/高度/位置类工程问题

## 10. 技术债 / 风险

- 无 git 仓库（无法版本回溯）
- 无自动化测试（compileTestJava NO-SOURCE）
- `FMLJavaModLoadingContext.get()` 弃用警告（1.20.1 标准用法）
- Curios 反射链依赖 API 形状（有兜底，未在真实 Curios 环境验证）
- 单人实测进度待回填（TestData 表）
- GuildDataSyncPacket 为聚合大包（任务+冒险者数据合并，V1.2 可拆分）

## 11. 策划设计问题（供复盘）

- 任务链与每日任务共用任务池，可能同一任务同日出现在两处（UI 已去重，接受校验允许其一）
- TRANSPORT 任务原以世界出生点为送达目标，公会成立后可考虑改为公会坐标（见 v1.1_design_questions）
- EXPLORE 为生物群系精确匹配，自定义群系兼容性待扩展（标签匹配）
- 声望无消耗渠道（V1.0 只做"累积+解锁"），V1.2 可加声望商品/服务

## 12. V1.1 完成度

功能层面完成（章节/事件/对话/Lore/Endgame/冒险团/UI/网络），**验证层面进行中**（真实游玩回归、多人、Curios）。

## 13. Build

`gradlew build` = **PASS**。

