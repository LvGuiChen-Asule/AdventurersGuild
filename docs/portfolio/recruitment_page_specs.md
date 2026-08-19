# recruitment_page_specs — PPT / Figma 逐页制作规格

> Canvas：1920 × 1080（16:9）｜风格：Fantasy RPG × Modern Game Portfolio
> 关键词：Dark Fantasy / Guild / Adventure / UI Panel / Gold Accent（#C9A227）/ Cyan Accent（#35C4D9）
> 每页原则：1 个核心观点 + 1 个主要视觉 + 3～5 个辅助信息。
> 字体建议：标题 Trajan Pro / Cinzel（幻想风）；正文 Inter / Noto Sans SC；代码与数据 Roboto Mono。

## 通用版式骨架

- 安全边距：左右 120px，上下 80px；
- 顶部标题区：y=60–140，标题字号 54–64，主色 Gold；
- 内容区：y=180–920；
- 底部页脚：y=1000，页码 + 章节名 + "V1.1 — IN DEVELOPMENT" 等版本徽标；
- 背景：深色渐变（#12100D → #1B1712），左下/右下叠暗金色装饰线或公会徽记剪影；
- 版本徽标组件：圆角胶囊，底色按等级（VERIFIED=绿 #3FA46B / IMPLEMENTED=金 / IN DEVELOPMENT=青 #35C4D9 / PLANNED=灰 #8A8A8A / CONCEPT=描边灰）。

---

## PAGE 01 — COVER

| 规格项 | 说明 |
| --- | --- |
| Page Number / Title | 01 / ADVENTURER'S GUILD |
| Core Message | AI Vibe Coding × Game Design × Playable Prototype |
| Layout | 全屏主视觉背景（公会大厅截图）→ 中央标题组 + 底部身份条 |
| Screenshot | 公会大厅全景：**已有 assets/AG_01_guild_hall.png**（备选 AG_13_hall_interior.png） |
| Diagram | 无 |
| Text | 标题（最大 96）/ 副标题 "AI Vibe Coding × Game Design"（48）/ 第三行 "Minecraft 1.20.1 · Playable Game Prototype"（30）/ 身份 "Game Designer / AI Prototype Developer"（26） |
| Data | 右下角 3 枚数据胶囊：80 Quests / 6 NPCs / 5 Chapters（VERIFIED） |
| Version Status | 主视觉 V1.1 IN DEVELOPMENT（截图已有） |
| Visual Hierarchy | 背景图 → 标题（最高对比）→ 身份条 → 数据胶囊 |
| Image Size | 1920×1080 全幅 |
| Font Size | 96 / 48 / 30 / 26 |
| Spacing | 标题组垂直间距 24–40 |
| Recommended UI Component | 全屏 Image + 中央 Text Stack + 底部 Capsule 行 |

## PAGE 02 — PROJECT SNAPSHOT

| 规格项 | 说明 |
| --- | --- |
| Page Number / Title | 02 / I Built a Playable Game Prototype |
| Core Message | 这是可构建、可进游戏的 Prototype，不是文档 |
| Layout | 左 40%：一句话结论 + 3 条解读；右 60%：2×3 大型数据卡网格 |
| Screenshot | 可放 Excel Quest 表截图（已有）作为背景水印（10% 透明度） |
| Diagram | 无 |
| Text | 标题 60；结论句 30；卡数值 72（Bold）+ 标签 22 |
| Data | 86 Files / 7,176 LOC / 80 Quests / 5 Chapters / 6 Dialogues / 12 Lore / 3 Chains / 21 Sheets / Build PASS |
| Version Status | 每卡右上角 VERIFIED 徽标；Build 卡注"记录 PASS" |
| Visual Hierarchy | 结论句 → 数据卡（数值最大）→ 说明 |
| Image Size | 数据卡 300×260；水印 500×300 |
| Font Size | 60 / 72 / 22 / 30 |
| Spacing | 卡间距 24，卡内 16 |
| Recommended UI Component | Stat Card Grid（2 行 × 3 列） |

## PAGE 03 — WHY THIS GAME?

| 规格项 | 说明 |
| --- | --- |
| Page Number / Title | 03 / Why Adventurer's Guild? |
| Core Message | 我不是加内容，我是加一层 Adventure Layer |
| Layout | 上半：左右对比双栏（Minecraft vs Adventurer's Guild 流程）；下半：核心观点横幅 |
| Screenshot | 可选：原版日落远景（暂缺，可不放或留白）作左栏背景 |
| Diagram | 两列流程箭头：探索→战斗→生存→建造 ／ 探索→任务目标→风险判断→冒险→奖励→成长→新目标 |
| Text | 标题 60；栏标题 32；流程 26；核心观点 34（Gold 高亮） |
| Data | 无大数字；可引 15 World Events（世界回应）|
| Version Status | 世界回应 = V1.1 IN DEVELOPMENT |
| Visual Hierarchy | 对比流程（左右）→ 核心观点（底横幅） |
| Image Size | 双栏各 800×360 |
| Font Size | 60 / 32 / 26 / 34 |
| Spacing | 栏间距 60；观点横幅距上 60 |
| Recommended UI Component | Two-Column Comparison + Banner |

## PAGE 04 — GAMEPLAY LOOP

| 规格项 | 说明 |
| --- | --- |
| Page Number / Title | 04 / From Quest to Adventure |
| Core Message | 核心循环：接任务 → 判断风险 → 冒险 → 奖励 → 解锁 |
| Layout | 中央环形流程图（8 节点），右侧 3 条产出说明 |
| Screenshot | 无（纯图） |
| Diagram | 环形：Discover→Evaluate→Prepare→Adventure→Complete→Gold/EXP/Rep→Unlock→New Adventure |
| Text | 标题 60；节点 28；右侧说明 26 |
| Data | 金币/EXP/声望三产出（VERIFIED）；解锁=品质槽/任务链/章节/Endgame |
| Version Status | 循环主体 V1.0；Endgame 解锁 V1.1 |
| Visual Hierarchy | 环形图（主视觉）→ 右侧产出卡 |
| Image Size | 环 900×900 |
| Font Size | 60 / 28 / 26 |
| Spacing | 环与说明间距 80 |
| Recommended UI Component | Circular Flow Diagram（Figma Auto Layout 圆周排列） |

## PAGE 05 — CASE STUDY 01｜Quest = Player Decision

| 规格项 | 说明 |
| --- | --- |
| Page Number / Title | 05 / Case 01｜Quest = Player Decision |
| Core Message | 任务制造取舍：同一小时，接哪个任务？ |
| Layout | 左：任务卡（hunt_witches 真实数据）+ 决策树；右：任务详情截图 |
| Screenshot | Quest Detail UI：**已有 assets/AG_04_quest_detail.png**；任务大厅 AG_03_quest_board.png |
| Diagram | 决策分支：风险 / 收益 / 战略（任务链第 3 步） |
| Text | 标题 54；字段标签 24；数值 30 |
| Data | HUNT / RARE×2.0 / 3 只女巫 / 110G·75EXP·25声望基础 / 限时 3600s / 等级≥3 / 任务链 step 3 |
| Version Status | 任务数据 V1.0 VERIFIED；UI 截图已有 |
| Visual Hierarchy | 任务卡（左主）→ 决策树（右） |
| Image Size | 任务卡 700×520；截图区 600×520 |
| Font Size | 54 / 24 / 30 |
| Spacing | 左右间距 60 |
| Recommended UI Component | Case Card + Decision Tree |

## PAGE 06 — DATA-DRIVEN QUEST

| 规格项 | 说明 |
| --- | --- |
| Page Number / Title | 06 / From Design to Data |
| Core Message | 策划不只在文档，直接落到数据和运行时 |
| Layout | 上半：水平五步流程（Design→Data→Runtime→Playtest→Iteration）；下半：三栏证据（Excel 截图 / JSON 代码块 / Java 模型名） |
| Screenshot | Excel 表截图（**已有**：Quest.png 等 9 张，选用 Quest/QuestPool） |
| Diagram | 五步流程箭头 |
| Text | 标题 54；流程 26；代码块 20（Roboto Mono） |
| Data | Excel 21 表；JSON 80 任务；"加任务=加 JSON，零代码改动" |
| Version Status | 全链路 VERIFIED |
| Visual Hierarchy | 流程（上）→ 证据三栏（下，Excel 图最醒目） |
| Image Size | 流程条 1600×140；证据卡 480×420 |
| Font Size | 54 / 26 / 20 |
| Spacing | 上下区间距 60 |
| Recommended UI Component | Step Bar + Evidence Cards |

## PAGE 07 — CASE STUDY 02｜NPC as World Interface

| 规格项 | 说明 |
| --- | --- |
| Page Number / Title | 07 / Case 02｜NPC as World Interface |
| Core Message | NPC 是世界的接口：行为 → 对话 → 任务 → 决策 |
| Layout | 左：NPC 信息流（纵向）；右：NPC / 对话截图 |
| Screenshot | 对话 UI：**已有 assets/AG_05_dialogue.png**；NPC：AG_02_npc.png |
| Diagram | NPC → Dialogue → Quest → World Info → Player Decision 纵向流 |
| Text | 标题 54；6 NPC 名单 24；信息流 26 |
| Data | 6 NPC（艾琳/罗德/米娅/格雷/伊莱恩/塞拉斯）；11 节点 23 分支；条件 8 类 |
| Version Status | 醒目徽标：**V1.1 — IN DEVELOPMENT** |
| Visual Hierarchy | 信息流（左主）→ 视觉证据（右） |
| Image Size | 信息流 760×760；截图区 720×620 |
| Font Size | 54 / 26 / 24 |
| Spacing | 左右间距 60 |
| Recommended UI Component | Vertical Flow + Screenshot Placeholder |

## PAGE 08 — CASE STUDY 03｜Guild Relics

| 规格项 | 说明 |
| --- | --- |
| Page Number / Title | 08 / Case 03｜Guild Relics |
| Core Message | 第二成长线：饰品改变玩法，不是堆属性 |
| Layout | 左：设计关系链（纵向箭头）；右：5 饰品卡网格 + 扩展方向区（灰色标注 PLANNED） |
| Screenshot | 饰品截图：**已有 assets/AG_08_relic.png** |
| Diagram | Quest→Reputation→Exploration→World Events→Lore→Relics→New Gameplay→Endgame |
| Text | 标题 54；饰品名 28；效果 24 |
| Data | 5 件：EXP+5% / 挖掘+5% / 伤害+5% / 移速+5% / 金币+5% |
| Version Status | 5 件 = IMPLEMENTED（V1.0）；扩展（传送卷轴/龙息容器等）= V1.2/V2.0 CONCEPT（灰显） |
| Visual Hierarchy | 关系链（左）→ 已实现饰品（右亮色）→ 扩展（右灰） |
| Image Size | 链 640×760；饰品卡 300×240 ×2 列 |
| Font Size | 54 / 28 / 24 |
| Spacing | 卡间距 20；链与卡间距 60 |
| Recommended UI Component | Progression Chain + Item Card Grid |

## PAGE 09 — LONG-TERM PROGRESSION

| 规格项 | 说明 |
| --- | --- |
| Page Number / Title | 09 / From One Quest to Long-term Progression |
| Core Message | 一次任务影响玩家下一阶段能看到什么 |
| Layout | 中央三层金字塔/阶梯（Short→Mid→Long），右侧"一次任务的三种回报"卡 |
| Screenshot | 无 |
| Diagram | 三层阶梯图，每层标注版本徽标 |
| Text | 标题 54；层名 32；说明 24 |
| Data | Short：Gold/EXP（V1.0）；Mid：Reputation 0–3000 / Chapter / Unlock；Long：15 Events / 12 Lore / Endgame 24 |
| Version Status | V1.0 IMPLEMENTED → V1.1 IN DEVELOPMENT → V1.2 观察指标（PLANNED） |
| Visual Hierarchy | 阶梯图（主）→ 回报卡（辅） |
| Image Size | 阶梯 1000×700 |
| Font Size | 54 / 32 / 24 |
| Spacing | 层间距 16；右卡间距 60 |
| Recommended UI Component | Layered Pyramid + Tag Chips |

## PAGE 10 — AI VIBE CODING WORKFLOW

| 规格项 | 说明 |
| --- | --- |
| Page Number / Title | 10 / How I Build with AI |
| Core Message | AI is my development collaborator, not my design decision-maker |
| Layout | 左 55%：大型纵向流程图（10 节点）；右 45%：AI vs Me 双列分工 |
| Screenshot | Codex 会话截图：**已有 assets/AG_11_codex.png** |
| Diagram | Game Idea→Gameplay Design→System Breakdown→Prompt→Codex→Prototype→Playtest→AI Debug/Refactor→Iteration→Playable Game |
| Text | 标题 54；节点 24；分工 24；核心观点 34（Cyan 高亮） |
| Data | 证据：TASK-001→030 全记录（docs/v1.1_task_reports.md） |
| Version Status | 工作流本身 = 真实发生（VERIFIED 记录） |
| Visual Hierarchy | 流程图（左主）→ 分工表（右）→ 核心观点横幅（底） |
| Image Size | 流程图 820×840；分工表 720×560 |
| Font Size | 54 / 24 / 34 |
| Spacing | 左右间距 60；横幅距底 80 |
| Recommended UI Component | Vertical Process Flow + Two-Column Table + Quote Banner |

## PAGE 11 — CODEX CASE STUDY

| 规格项 | 说明 |
| --- | --- |
| Page Number / Title | 11 / Prompt → Prototype |
| Core Message | 一个真实案例：公会建筑从地下 Y=0 修复为地表生成 |
| Layout | 四段纵向流水：Prompt → Codex → Code Change → Game Result；右侧放公会大厅实景 |
| Screenshot | 修复后公会大厅实景：**已有 assets/AG_01_guild_hall.png**；前后对比图待补（可选） |
| Diagram | Prompt→Codex→Code Change→Game Result |
| Text | 标题 54；四段 24–26 |
| Data | 根因：Jigsaw 高度投影不可靠；方案：地表搜索+手动放置+clearTerrain+placeDoor+SavedData 坐标 |
| Version Status | 案例 = VERIFIED（QUESTION-003 + 代码 GuildWorldEvents + 用户实测） |
| Visual Hierarchy | 流水（左主）→ 游戏结果图（右） |
| Image Size | 流水 860×840；图区 700×600 |
| Font Size | 54 / 26 / 24 |
| Spacing | 段间距 20；左右间距 60 |
| Recommended UI Component | Step Timeline + Before/After Placeholder |

## PAGE 12 — AI ITERATION

| 规格项 | 说明 |
| --- | --- |
| Page Number / Title | 12 / From Prototype to Better Prototype |
| Core Message | AI 降低试错成本：4 个真实 Playtest→Fix 循环 |
| Layout | 4 行横向卡片（V1→Problem→AI Fix→V2），每行一个迭代 |
| Screenshot | 可选：修复前问题截图（未留存，可省）；修复后实景用 AG_01/AG_13 |
| Diagram | V1→Playtest→Problem→AI Analysis→Codex→V2 循环箭头 |
| Text | 标题 54；迭代标题 28；描述 22 |
| Data | Y=0 地下 / 门掉落 / NPC 房顶 / 地形穿模 四条真实记录 |
| Version Status | 全部 VERIFIED（开发日志 + 用户实测反馈） |
| Visual Hierarchy | 迭代卡（主）→ 循环口号（底） |
| Image Size | 卡 1600×160 ×4 |
| Font Size | 54 / 28 / 22 |
| Spacing | 卡间距 16 |
| Recommended UI Component | Iteration Row Cards（V1→V2 用箭头连接） |

## PAGE 13 — UI / UX

| 规格项 | 说明 |
| --- | --- |
| Page Number / Title | 13 / Designing for Player Decisions |
| Core Message | 每张 UI 回答"玩家在这里需要做什么" |
| Layout | 2×3 网格：6 张 UI 截图 + 每张下方一句"玩家决策" |
| Screenshot | 任务大厅 AG_03 / 详情 AG_04 / 对话 AG_05 / 冒险者 AG_07 / 档案 AG_15（商店暂缺，可用 Excel Shop 图替代） |
| Diagram | 无（截图为主） |
| Text | 标题 54；UI 名 24；决策句 20 |
| Data | 10 页面；品质色双编码；锁定原因显性化 |
| Version Status | UI = V1.1 IN DEVELOPMENT（已实现，截图已就位） |
| Visual Hierarchy | 截图网格（主）→ 决策句（每格底部） |
| Image Size | 卡 520×440 ×6 |
| Font Size | 54 / 24 / 20 |
| Spacing | 卡间距 24 |
| Recommended UI Component | Screenshot Grid + Caption |

## PAGE 14 — DESIGN → IMPLEMENTATION

| 规格项 | 说明 |
| --- | --- |
| Page Number / Title | 14 / From Design to Implementation |
| Core Message | 设计能落地：Player→System→Data→Runtime→Save→UI |
| Layout | 左 60%：纵向六层流程；右 40%：技术标签列表 |
| Screenshot | 可选：runServer 启动日志截图（待补；当前可用文档记录替代） |
| Diagram | Player→Game System→Data→Runtime→Save/Sync→UI |
| Text | 标题 54；层名 26；技术标签 22 |
| Data | Forge 47.4.22 / Java 17 / 12 网络包 / NBT+SavedData / runServer Done 7.456s |
| Version Status | 架构 VERIFIED；NPC/对话层 V1.1 IN DEVELOPMENT |
| Visual Hierarchy | 六层流程（左主）→ 技术标签（右） |
| Image Size | 流程 980×820；标签区 640×680 |
| Font Size | 54 / 26 / 22 |
| Spacing | 层间距 12；左右间距 60 |
| Recommended UI Component | Vertical Pipeline + Tag Cloud |

## PAGE 15 — PLAYABLE DEMO

| 规格项 | 说明 |
| --- | --- |
| Page Number / Title | 15 / From Idea to Playable Game |
| Core Message | 可玩证据：游戏真实运行、真实加载、真实游玩 |
| Layout | 左：Demo 流程（公会→NPC→任务→冒险→奖励→成长）；右：证据堆（大截图/视频封面/文档链接/QR 位） |
| Screenshot | 大截图：**已有** AG_01/AG_03/AG_04/AG_05；视频封面**待录**（暂缓）；QR 位标注"GitHub：暂无（不伪造）" |
| Diagram | Demo Flow 纵向流程 |
| Text | 标题 54；流程 24；证据标签 22 |
| Data | runServer 冒烟：80 任务加载 Done 7.456s；用户实测反馈记录 |
| Version Status | 可玩性 VERIFIED；媒体资产待补 |
| Visual Hierarchy | 大截图位（右主）→ Demo 流程（左） |
| Image Size | 截图位 1000×640；流程 620×640 |
| Font Size | 54 / 24 / 22 |
| Spacing | 左右间距 60 |
| Recommended UI Component | Flow + Media Placeholder Stack |

## PAGE 16 — WHY ME

| 规格项 | 说明 |
| --- | --- |
| Page Number / Title | 16 / Why Me? |
| Core Message | Game Designer who builds with AI |
| Layout | 三段横排（01 Game Understanding / 02 AI Prototyping / 03 System Design）+ 底部结语横幅 |
| Screenshot | 无 |
| Diagram | 无（三卡为主） |
| Text | 标题 60；卡标题 34；正文 24；结语 40（Cyan/Gold 高亮） |
| Data | 引用页码：P05/P07/P09（理解）、P10–P12（AI）、P06/P14（系统） |
| Version Status | 全部引用真实章节 |
| Visual Hierarchy | 三卡（主）→ 结语（底横幅） |
| Image Size | 卡 480×560 ×3 |
| Font Size | 60 / 34 / 24 / 40 |
| Spacing | 卡间距 40；横幅距底 80 |
| Recommended UI Component | Three Column Cards + Closing Banner |

---

## 交付顺序建议（按依赖）

1. 12 张游戏截图已收录（assets/AG_*.png），9 张 Excel 截图已收录 → P01/P02/P05/P06/P07/P08/P13/P15 可直接制作；
2. 剩余两张（09 世界事件、12 Build）不阻塞：P09 用 AG_07/AG_15 替代，P02/P14 用文档记录替代；
3. 录制 2–4 分钟 Demo（暂缓）→ 后续替换 P15 视频/GIF 封面；
4. 按本规格逐页搭建 PPT/Figma（全部 16 页均可开工）；
5. 前后对比图（Y=0 地下 vs 地表）为可选加分项，不阻塞交付。
