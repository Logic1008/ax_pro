# OpenSpec 使用说明

面向使用 OpenSpec 进行规范驱动开发的 AI 编码助手指南。

## TL;DR 快速清单

- 先查现有内容：`openspec spec list --long`、`openspec list`（全文检索才用 `rg`）
- 判断范围：新增能力 vs 修改现有能力
- 选唯一的动词开头 `change-id`：kebab-case，动词前缀（如 `add-`、`update-`、`remove-`、`refactor-`）
- 搭脚手架：`proposal.md`、`tasks.md`、`design.md`（需要时），以及受影响能力的增量 spec
- 写增量：使用 `## ADDED|MODIFIED|REMOVED|RENAMED Requirements`；每条 Requirement 至少有一个 `#### Scenario:`
- 校验：`openspec validate [change-id] --strict` 并修复所有问题
- 申请批准：提案获批前不要开始实现

## 三阶段流程

### 阶段 1：创建变更
以下情况需要创建 proposal：
- 增加功能或能力
- 破坏性变更（API、Schema 等）
- 架构或模式调整
- 性能优化（会改变行为）
- 安全模式变更

触发示例：
- “帮我创建一个变更提案”
- “帮我规划一个变更”
- “我想做一个 spec proposal/change/spec”

模糊匹配建议：
- 语句包含：`proposal`、`change`、`spec`
- 且包含：`create`、`plan`、`make`、`start`、`help`

可跳过 proposal 的情况：
- Bug 修复（恢复既定行为）
- 拼写、格式、注释
- 非破坏性的依赖升级
- 配置改动
- 针对既有行为的测试

**工作流**
1. 阅读 `openspec/project.md`、运行 `openspec list` 和 `openspec list --specs` 获取上下文。
2. 选一个唯一的动词开头 `change-id`，在 `openspec/changes/<id>/` 下创建 `proposal.md`、`tasks.md`、可选 `design.md`，以及受影响能力的 spec 增量。
3. 用 `## ADDED|MODIFIED|REMOVED Requirements`（必要时含 `RENAMED`）撰写增量，每条 Requirement 至少有一个 `#### Scenario:`。
4. 运行 `openspec validate <id> --strict` 并修复所有问题后再分享提案。

### 阶段 2：实现变更
按 TODO 逐项完成：
1. **阅读 proposal.md** - 明确要做什么
2. **阅读 design.md**（如有） - 查看技术决策
3. **阅读 tasks.md** - 获取实现清单
4. **按顺序实现** - 逐项完成任务
5. **确认完成** - 确保 tasks.md 中的任务都已完成
6. **更新清单** - 全部完成后将每项标记为 `- [x]`
7. **审批门禁** - 提案获批前不要开始实现

### 阶段 3：归档变更
部署后单独提 PR：
- 移动 `changes/[name]/` → `changes/archive/YYYY-MM-DD-[name]/`
- 如有能力变化，更新 `specs/`
- 仅工具类变更用 `openspec archive <change-id> --skip-specs --yes`
- 运行 `openspec validate --strict` 确认归档通过

## 开工前

**上下文检查表：**
- [ ] 阅读相关 spec：`specs/[capability]/spec.md`
- [ ] 查看 `changes/` 中的进行中变更是否冲突
- [ ] 阅读 `openspec/project.md` 了解约定
- [ ] 运行 `openspec list` 查看活跃变更
- [ ] 运行 `openspec list --specs` 查看已有能力

**写 spec 前：**
- 总是先检查能力是否已存在
- 优先修改现有 spec，避免重复
- 用 `openspec show [spec]` 查看当前内容
- 需求不清时，先问 1–2 个澄清问题再搭脚手架

### 搜索指南
- 枚举 spec：`openspec spec list --long`（脚本用 `--json`）
- 枚举变更：`openspec list`（或已废弃的 `openspec change list --json`）
- 查看详情：
  - Spec：`openspec show <spec-id> --type spec`（可加 `--json`）
  - Change：`openspec show <change-id> --json --deltas-only`
- 全文搜索：`rg -n "Requirement:|Scenario:" openspec/specs`

## 快速开始

### CLI 命令

```bash
# 常用命令
openspec list                  # 列出活跃变更
openspec list --specs          # 列出规格
openspec show [item]           # 查看变更或 spec
openspec validate [item]       # 校验变更或 spec
openspec archive <change-id> [--yes|-y]   # 部署后归档（自动化请加 --yes）

# 项目管理
openspec init [path]           # 初始化 OpenSpec
openspec update [path]         # 更新说明文件

# 交互模式
openspec show                  # 交互选择查看
openspec validate              # 批量校验模式

# 调试
openspec show [change] --json --deltas-only
openspec validate [change] --strict
```

### 命令参数

- `--json` - 机器可读输出
- `--type change|spec` - 指定类型
- `--strict` - 全量校验
- `--no-interactive` - 关闭交互
- `--skip-specs` - 归档时跳过 spec 更新
- `--yes`/`-y` - 无需确认（适合非交互场景）

## 目录结构

```
openspec/
├── project.md              # 项目约定
├── specs/                  # 已落地的真实能力
│   └── [capability]/       # 单一能力
│       ├── spec.md         # 需求与场景
│       └── design.md       # 技术模式
├── changes/                # 提案/待实现
│   ├── [change-name]/
│   │   ├── proposal.md     # 为什么/做什么/影响
│   │   ├── tasks.md        # 实施清单
│   │   ├── design.md       # 技术决策（可选，见标准）
│   │   └── specs/          # 增量 spec
│   │       └── [capability]/
│   │           └── spec.md # ADDED/MODIFIED/REMOVED
│   └── archive/            # 已完成的变更
```

## 创建变更提案

### 决策树

```
新需求？
├─ 修复 Bug、恢复既有行为？ → 直接修
├─ 拼写/格式/注释？ → 直接修
├─ 新功能/能力？ → 创建 proposal
├─ 破坏性变更？ → 创建 proposal
├─ 架构变更？ → 创建 proposal
└─ 不确定？ → 创建 proposal（更安全）
```

### 提案结构

1. **创建目录：** `changes/[change-id]/`（kebab-case，动词开头，唯一）
2. **撰写 proposal.md：**
```markdown
# Change: [简述变更]

## Why
[1-2 句问题/机会]

## What Changes
- [变更要点]
- [破坏性变更标注 **BREAKING**]

## Impact
- Affected specs: [能力列表]
- Affected code: [关键文件/系统]
```
3. **创建 spec 增量：** `specs/[capability]/spec.md`
```markdown
## ADDED Requirements
### Requirement: New Feature
The system SHALL provide...

#### Scenario: Success case
- **WHEN** user performs action
- **THEN** expected result

## MODIFIED Requirements
### Requirement: Existing Feature
[完整修改后的需求]

## REMOVED Requirements
### Requirement: Old Feature
**Reason**: [移除原因]
**Migration**: [处理方式]
```
如果有多个能力，按能力分目录：`changes/<id>/specs/<capability>/spec.md`。

4. **创建 tasks.md：**
```markdown
## 1. Implementation
- [ ] 1.1 Create database schema
- [ ] 1.2 Implement API endpoint
- [ ] 1.3 Add frontend component
- [ ] 1.4 Write tests
```

5. **需要时创建 design.md：** 满足以下任一条件才创建，否则省略：
- 跨系统/跨模块或新增架构模式
- 新外部依赖或重要数据模型变更
- 安全、性能或迁移复杂度高
- 存在不确定性，需要先定技术方案

最小 `design.md` 模板：
```markdown
## Context
[背景、约束、干系人]

## Goals / Non-Goals
- Goals: [...]
- Non-Goals: [...]

## Decisions
- Decision: [做什么、为何]
- Alternatives considered: [方案与理由]

## Risks / Trade-offs
- [风险] → 缓解措施

## Migration Plan
[步骤与回滚]

## Open Questions
- [...]
```

## Spec 文件格式

### 关键：Scenario 的写法

**正确示例**（用 `####` 作为标题）：
```markdown
#### Scenario: User login success
- **WHEN** valid credentials provided
- **THEN** return JWT token
```

**错误示例**（不要用列表或粗体充当标题）：
```markdown
- **Scenario: User login**  ❌
**Scenario**: User login     ❌
### Scenario: User login      ❌
```

每个 Requirement 至少要有一个 Scenario。

### Requirement 描述
- 使用 SHALL/MUST 等强制语气（非强制用 should/may）

### 增量操作

- `## ADDED Requirements` - 新增能力
- `## MODIFIED Requirements` - 修改现有行为
- `## REMOVED Requirements` - 废弃功能
- `## RENAMED Requirements` - 仅更名

#### ADDED vs MODIFIED
- ADDED：引入可独立理解的新能力/关注点，优先使用 ADDED 以避免丢失原行为。
- MODIFIED：改变已有需求的行为、范围或验收标准。务必粘贴完整的 Requirement（标题+全部场景），因为归档时会用新内容覆盖旧内容。
- RENAMED：仅改名字；若同时改行为，需 RENAMED（名字）+ MODIFIED（内容）。

编写 MODIFIED 的正确姿势：
1) 找到 `openspec/specs/<capability>/spec.md` 里的原 Requirement。
2) 复制完整块（`### Requirement: ...` 到所有场景）。
3) 放到 `## MODIFIED Requirements` 下再编辑。
4) 标题保持一致（忽略空白），保留至少一个 `#### Scenario:`。

RENAMED 示例：
```markdown
## RENAMED Requirements
- FROM: `### Requirement: Login`
- TO: `### Requirement: User Authentication`
```

## 故障排查

### 常见错误

**“Change must have at least one delta”**
- 确认 `changes/[name]/specs/` 下有 .md
- 确认文件使用了 `## ADDED|MODIFIED...` 头

**“Requirement must have at least one scenario”**
- 场景标题必须是 `#### Scenario:`
- 不要用列表或粗体标题

**场景解析静默失败**
- 严格按照 `#### Scenario: Name` 格式
- 用 `openspec show [change] --json --deltas-only` 调试

### 校验提示

```bash
# 全量校验
openspec validate [change] --strict

# 调试增量解析
openspec show [change] --json | jq '.deltas'

# 检查特定 Requirement
openspec show [spec] --json -r 1
```

## Happy Path 示例脚本

```bash
# 1) 探索当前状态
openspec spec list --long
openspec list
# 可选全文检索：
# rg -n "Requirement:|Scenario:" openspec/specs
# rg -n "^#|Requirement:" openspec/changes

# 2) 选择 change id 并搭脚手架
CHANGE=add-two-factor-auth
mkdir -p openspec/changes/$CHANGE/{specs/auth}
printf "## Why\n...\n\n## What Changes\n- ...\n\n## Impact\n- ...\n" > openspec/changes/$CHANGE/proposal.md
printf "## 1. Implementation\n- [ ] 1.1 ...\n" > openspec/changes/$CHANGE/tasks.md

# 3) 添加增量（示例）
cat > openspec/changes/$CHANGE/specs/auth/spec.md << 'EOSPEC'
## ADDED Requirements
### Requirement: Two-Factor Authentication
Users MUST provide a second factor during login.

#### Scenario: OTP required
- **WHEN** valid credentials are provided
- **THEN** an OTP challenge is required
EOSPEC

# 4) 校验
openspec validate $CHANGE --strict
```

## 多能力示例

```
openspec/changes/add-2fa-notify/
├── proposal.md
├── tasks.md
└── specs/
    ├── auth/
    │   └── spec.md   # ADDED: Two-Factor Authentication
    └── notifications/
        └── spec.md   # ADDED: OTP email notification
```

auth/spec.md
```markdown
## ADDED Requirements
### Requirement: Two-Factor Authentication
...
```

notifications/spec.md
```markdown
## ADDED Requirements
### Requirement: OTP Email Notification
...
```

## 最佳实践

### 先保持简单
- 默认新增代码 <100 行
- 单文件实现，除非确有需要
- 避免无理由的复杂框架
- 选择可靠、成熟的模式

### 触发加复杂度的条件
仅在以下情况下增加复杂度：
- 有性能数据证明当前方案不足
- 明确的规模需求（>1000 用户，>100MB 数据等）
- 已经存在多个用例需要抽象

### 明确引用
- 代码位置用 `file.ts:42`
- spec 引用用 `specs/auth/spec.md`
- 关联变更/PR 请显式链接

### 能力命名
- 使用动宾短语：`user-auth`、`payment-capture`
- 单一用途
- 10 分钟可理解
- 若描述需要 “和/以及”，考虑拆分

### change-id 命名
- kebab-case，简短、描述性：`add-two-factor-auth`
- 优先动词前缀：`add-`、`update-`、`remove-`、`refactor-`
- 保证唯一；冲突时追加 `-2`、`-3` 等

## 工具选择指南

| Task | Tool | Why |
|------|------|-----|
| 按模式找文件 | Glob | 模式匹配快 |
| 搜索代码内容 | Grep | 规范化正则搜索 |
| 读取特定文件 | Read | 直接访问 |
| 探索未知范围 | Task | 多步骤调查 |

## 错误恢复

### 变更冲突
1. 运行 `openspec list` 看活跃变更
2. 检查是否有重叠 spec
3. 协调变更负责人
4. 必要时合并提案

### 校验失败
1. 使用 `--strict` 运行
2. 查看 JSON 输出定位问题
3. 检查增量格式
4. 确认场景格式正确

### 上下文缺失
1. 优先阅读 project.md
2. 检查相关 spec
3. 查看近期归档
4. 不清楚就提问

## 快捷参考

### 阶段标识
- `changes/` - 提案/未落地
- `specs/` - 已落地的真实行为
- `archive/` - 已完成的变更

### 文件用途
- `proposal.md` - 为什么/做什么
- `tasks.md` - 实施步骤
- `design.md` - 技术决策
- `spec.md` - 需求与行为

### 核心命令
```bash
openspec list              # 有什么进行中的变更？
openspec show [item]       # 查看详情
openspec validate --strict # 是否通过校验？
openspec archive <change-id> [--yes|-y]  # 归档（自动化请加 --yes）
```

牢记：specs = 真实，changes = 提案。请保持两者同步。
