# recruitment_resume_project_entry — 简历项目经历（可直接复制）

## 项目经历

**Adventurer's Guild — 任务驱动型冒险成长游戏原型（Minecraft Mod）**

角色：Game Designer / AI Prototype Developer（独立完成策划、数值、UI 与全部开发）
时间：2026-08（V0.1 → V1.1）
平台：Minecraft 1.20.1 · Forge 47.4.22 · Java 17 · Gradle 8.8

**一句话**：使用 AI（Codex）协作开发的可玩游戏原型——给 Minecraft 原版生存流程增加一层"任务驱动的冒险层"。

**项目数据（VERIFIED）**：

- 代码：86 个 Java 文件 / 约 7,176 行；构建 PASS（记录于任务报告与项目审计）
- 内容：80 个数据驱动任务（9 类型 × 5 品质）、5 章节主线、6 名 NPC + 数据驱动对白、12 条 Lore、15 个世界事件、3 条任务链、5 件饰品、3 个商店
- 数据：21 张 Excel 配表，运行时 JSON 驱动（改数据不改代码）
- 系统：任务/成长/声望/经济/装备/每日任务板/NPC/对话/章节/世界事件/冒险团/Endgame
- 技术：服务器权威判定、网络协议 v2（12 包）、NBT Capability + SavedData 持久化、事件驱动性能设计

**我的职责**：

1. 问题定义：识别 Minecraft"高自由度但缺少持续任务驱动与世界反馈"的体验缺口；
2. 系统设计：核心循环、任务=决策、NPC=世界界面、三层成长（Gold/EXP → 声望/章节 → 事件/Lore/Endgame）；
3. 数值设计：等级/声望曲线、品质倍率 1.0–5.0、奖励公式（基础×品质×装备）、金币产出与回收模型；
4. 数据设计：21 表配表 → JSON，字段可直接交付程序；
5. AI 协作开发：全程使用 Codex 完成代码生成、调试、重构；我负责玩法目标、需求、Prompt、Playtest 与最终决策；
6. 迭代：基于真实游玩反馈完成 4 轮修复（建筑地下生成/门掉落/NPC 站位/地形穿模），有完整开发日志。

**可验证性**：

- 项目文档：29 章策划集 + 招聘版作品集（docs/portfolio/）
- 开发记录：docs/development_log.md、docs/v1.1_task_reports.md（TASK-001→030）
- 可运行：服务器冒烟 80 任务全部加载（Done 7.456s），玩家已在 Java 版实测

**注意（诚实标注，勿删）**：

- 游戏截图/演示视频：待补拍（文档脚本已就绪）
- GitHub：https://github.com/LvGuiChen-Asule/AdventurersGuild（公开仓库，含全部源码与作品集）
- 多人（2/5/10 人）与 Curios 环境：尚未实测

## 简历写法示例（单条 Bullet）

> **Adventurer's Guild（Minecraft 1.20.1 Forge 游戏原型）** — Game Designer / AI Prototype Developer，独立完成设计与开发：80 个数据驱动任务、5 章节主线、6 NPC 与数据驱动对话、15 世界事件、12 Lore、3 商店、5 饰品、21 表配表；全程使用 Codex 协作开发并基于真实游玩反馈迭代 4 轮（从地下生成修复到地表、门掉落修复、NPC 站位、地形清理）；构建 PASS、服务器冒烟 80 任务全部加载。
