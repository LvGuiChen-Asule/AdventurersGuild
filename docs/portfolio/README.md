# 《Adventurer's Guild 游戏系统策划集》

基于真实开发的 Minecraft 1.20.1 Forge Mod 项目（`adventurersguild`）整理的作品集文档。

> 原则：所有内容以代码与资源为准；规划内容一律标注版本状态
> （V1.0 已实现 / V1.1 开发中 / V1.2 计划 / V2.0 构想）。

## 目录

### 招聘版作品集（AI 策划 / Vibe Coding 岗位）

| 文件 | 内容 |
| --- | --- |
| [recruitment_portfolio.md](recruitment_portfolio.md) | 16 页招聘作品集完整内容设计（含最终审计） |
| [recruitment_page_specs.md](recruitment_page_specs.md) | PPT / Figma 逐页制作规格（1920×1080） |
| [recruitment_screenshot_checklist.md](recruitment_screenshot_checklist.md) | 截图证据清单（已有 9 张 Excel 图 + 待拍清单） |
| [recruitment_demo_script.md](recruitment_demo_script.md) | 2–4 分钟 Demo 分镜 |
| [recruitment_hr_reading_path.md](recruitment_hr_reading_path.md) | HR 3 分钟阅读路径 |
| [recruitment_resume_project_entry.md](recruitment_resume_project_entry.md) | 简历项目条目（可直接复制） |

| 章节 | 文件 | 内容 |
| --- | --- | --- |
| 00 | [00_project_audit.md](00_project_audit.md) | 项目真实功能基线审计 |
| 01 | [01_project_overview.md](01_project_overview.md) | 项目概述与定位 |
| 02 | [02_design_philosophy.md](02_design_philosophy.md) | 核心设计理念 |
| 03 | [03_core_loop.md](03_core_loop.md) | 核心玩法循环 |
| 04 | [04_world_design.md](04_world_design.md) | 世界结构与公会建筑 |
| 05 | [05_main_story.md](05_main_story.md) | 主线系统与章节 |
| 06 | [06_side_quest.md](06_side_quest.md) | 支线任务系统 |
| 07 | [07_quest_system.md](07_quest_system.md) | 任务品质与每日任务 |
| 08 | [08_progression.md](08_progression.md) | 冒险者成长系统 |
| 09 | [09_reputation.md](09_reputation.md) | 公会声望系统 |
| 10 | [10_economy.md](10_economy.md) | 公会经济系统 |
| 11 | [11_npc.md](11_npc.md) | NPC 系统 |
| 12 | [12_guild_world.md](12_guild_world.md) | 公会建筑与生成 |
| 13 | [13_world_events.md](13_world_events.md) | 世界事件系统 |
| 14 | [14_lore.md](14_lore.md) | Lore / 档案系统 |
| 15 | [15_relics.md](15_relics.md) | 饰品 / 遗物系统 |
| 16 | [16_endgame.md](16_endgame.md) | Endgame 系统 |
| 17 | [17_party.md](17_party.md) | 冒险团系统 |
| 18 | [18_uiux.md](18_uiux.md) | UI/UX 策划 |
| 19 | [19_numerical_design.md](19_numerical_design.md) | 数值策划 |
| 20 | [20_data_tables.md](20_data_tables.md) | 数据表设计 |
| 21 | [21_technical_architecture.md](21_technical_architecture.md) | 技术架构 |
| 22 | [22_network.md](22_network.md) | 网络与数据安全 |
| 23 | [23_performance.md](23_performance.md) | 性能设计 |
| 24 | [24_version_plan.md](24_version_plan.md) | 版本规划 |
| 25 | [25_demo.md](25_demo.md) | Demo 演示流程 |
| 26 | [26_screenshot_plan.md](26_screenshot_plan.md) | 作品集截图规划 |
| 27 | [27_project_retro.md](27_project_retro.md) | 项目复盘 |
| 28 | [28_personal_design_summary.md](28_personal_design_summary.md) | 个人策划能力总结 |

## 事实依据

- 代码：`src/main/java`（86 个 Java 文件，约 7176 行）
- 数据：`src/main/resources/data/adventurersguild/`（80 任务 / 5 章 / 6 对话 / 12 Lore / 3 商店 / 3 任务链 / 22 任务池条目 / 5 装备）
- 配表：`data/AdventurersGuild_Data.xlsx`（21 个工作表）
- 构建：`gradlew build` = PASS
