# recruitment_hr_reading_path — HR 3 分钟阅读路径

> 目标：让第一次打开作品集的人，在 3 分钟内得到五个结论：
> ① 这是真实项目 ② 这个人理解游戏玩法 ③ 这个人能使用 AI ④ 这个人能把设计变成 Prototype ⑤ 这个人会测试和迭代。

## 0～30 秒：HR 应该看到什么？

**看到的**：

- 封面："ADVENTURER'S GUILD — AI Vibe Coding × Game Design — Minecraft 1.20.1 · Playable Game Prototype"
- 主视觉：公会大厅真实游戏截图（待拍，拍前用"Prototype 预览位"）
- 页面 02 数据卡：86 Java Files / 7,176 LOC / 80 Quests / 21 Data Sheets / Build PASS

**形成的第一印象**：这是一个**能运行的游戏原型**，不是文档堆。

## 30～60 秒：HR 应该理解什么？

**看到的**：PAGE 03–04

- "Why": 不给 Minecraft 加内容，而是加一层 Adventure Layer；
- 核心循环图：Discover Quest → Risk/Reward → Prepare → Adventure → Complete → Gold/EXP/Rep → Unlock → New Adventure。

**理解的**：这个人**从玩家体验问题出发设计**，并且能画出完整循环。

## 1～2 分钟：HR 应该相信什么？

**看到的**：PAGE 05–09（三个 Case Study + 长线成长）

- Case 01：任务=玩家决策（hunt_witches 的真实 Objective/Condition/Reward/Unlock 数据）；
- Case 02：NPC=世界界面（V1.1 自研轻量 Framework + 数据驱动对话）；
- Case 03：饰品=第二成长线（5 件已实现 + 扩展为构想，版本清晰）；
- 长线成长：Short（Gold/EXP）→ Mid（声望/章节）→ Long（事件/Lore/Endgame）。

**相信的**：这个人**真的做完了系统设计和内容**，且分得清"已实现/开发中/规划"。

## 2～3 分钟：HR 应该被什么证据说服？

**看到的**：PAGE 10–15

- AI 工作流：AI 负责 Code/Debug/Refactor，我负责 Design/Judgment/Decision；
- Codex 真实案例：公会建筑从地下 Y=0 → 地表生成（Prompt→Code→Game Result 全链）；
- 迭代证据：4 个 Playtest→Fix 循环（门掉落/NPC 房顶/地形穿模）；
- 可玩证据：runServer 冒烟 80 任务加载 Done 7.456s + 用户实测记录；
- 简历入口页（recruitment_resume_project_entry.md）：直接复制进简历。

**被说服的**：这个人**用 AI 把一个想法快速变成了可玩原型，并持续迭代**——这正是 JD 要的人。

## 3 分钟之后：想深挖的人看哪里？

- 完整 29 章策划集：`docs/portfolio/00–28`（系统深度）；
- 代码证据：`src/main/java`（86 文件）、`data/adventurersguild`（JSON 数据）；
- 开发记录：`docs/development_log.md`、`docs/v1.1_task_reports.md`、`docs/v1.1_design_questions.md`；
- 页面规格：`recruitment_page_specs.md`（证明"连作品集本身都按规格做"）。

## 防翻车提示（务必遵守）

- 游戏截图未拍前：页面明确放"待拍"占位，不放网图/生成图冒充；
- 无 GitHub：不画 QR、不写链接，改为"项目文档齐全，可现场演示"；
- 版本徽标不混用：V1.1 内容必须标 IN DEVELOPMENT，V1.2/V2.0 必须标 PLANNED/CONCEPT。
