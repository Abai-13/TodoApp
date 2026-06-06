# TodoApp API Documentation

> 本文档记录 TodoApp 项目的所有类、字段和方法，按阶段逐步更新。

---

## 协作规则

1. **文件格式**：Markdown，文件名固定为 `docs/API.md`
2. **内容结构**：每个类单独一个章节，采用表格形式列出：
   - 类名 + 简要作用
   - 字段表格：字段名、类型、访问权限、作用
   - 方法表格：方法名、参数列表、返回类型、作用
3. **更新规则**：
   - 每完成一个阶段，更新 API.md 而非覆盖
   - 只追加本阶段新增的类或方法，不删除已有内容（除非方法被删除）
   - 如果某个类的方法有修改（参数变更、作用改变），在原条目旁注明 **（已修改）**
4. **私有成员**：私有字段也需列出，在访问权限列标注 `private`。私有方法一般不列出，除非被内部调用且其他类依赖其逻辑
5. **遗漏补充**：如发现漏掉公共类、方法或重要字段，提醒补充，后续生成应自动包含
6. **精简版**：当清单超过 150 行时，可生成精简版保存为 `docs/API_lite.md`，只保留核心类和常用方法
7. **版本记录**：每个阶段结束后，在 API.md 末尾追加 `<!-- stage X completed -->` 注释

---

## 项目结构

```
TodoApp/
├── src/              # .java 源文件
├── lib/              # 第三方 .jar 文件
│   ├── gson-2.14.0.jar
│   └── flatlaf-3.7.1.jar
├── data/             # 数据文件
│   └── todos.json
├── docs/             # 文档
│   └── API.md
├── MANIFEST.MF       # JAR 清单文件
├── TodoApp.jar       # 可运行 JAR 包
└── run.bat           # Windows 启动脚本
```

---

## 依赖库

| 库名 | 版本 | 作用 |
|------|------|------|
| Gson | 2.14.0 | JSON 序列化/反序列化，用于读写 todos.json |
| FlatLaf | 3.7.1 | Swing Look & Feel，实现苹果风格 UI |

---

## 类清单

### Todo — 数据模型类

> 包：`com.todoapp.model`  
> 作用：表示一条待办事项，所有字段通过 getter/setter 访问。Gson 序列化/反序列化时直接操作字段。

#### 字段

| 字段名 | 类型 | 访问权限 | 作用 |
|--------|------|----------|------|
| `id` | `String` | private | 唯一标识，使用 UUID 字符串 |
| `title` | `String` | private | 待办事项标题 |
| `completed` | `boolean` | private | 是否已完成 |
| `priority` | `String` | private | 优先级：高 / 中 / 低 |
| `category` | `String` | private | 分类：工作 / 个人 / 购物 等 |
| `remindTime` | `long` | private | 提醒时间（毫秒时间戳），-1 表示无提醒 |
| `reminded` | `boolean` | private | 是否已提醒过 |
| `createTime` | `Date` | private | 创建时间 |

#### 构造方法

| 方法名 | 参数列表 | 返回类型 | 作用 |
|--------|----------|----------|------|
| `Todo()` | 无 | — | 无参构造，自动生成 UUID id 和当前时间 createTime，设置默认值（未完成、中优先级、未分类、无提醒） |
| `Todo(String title)` | `title` — 标题 | — | 带标题构造，其余同无参构造 |
| `Todo(String id, String title, boolean completed, String priority, String category, long remindTime, boolean reminded, Date createTime)` | 全部字段 | — | 全参构造，允许从 JSON 反序列化时重建完整对象 |

#### 公共方法

| 方法名 | 参数列表 | 返回类型 | 作用 |
|--------|----------|----------|------|
| `getId()` | 无 | `String` | 获取唯一标识 |
| `setId(String id)` | `id` — 唯一标识 | `void` | 设置唯一标识 |
| `getTitle()` | 无 | `String` | 获取标题 |
| `setTitle(String title)` | `title` — 标题 | `void` | 设置标题 |
| `isCompleted()` | 无 | `boolean` | 获取是否已完成 |
| `setCompleted(boolean completed)` | `completed` — 是否完成 | `void` | 设置完成状态 |
| `getPriority()` | 无 | `String` | 获取优先级 |
| `setPriority(String priority)` | `priority` — 优先级 | `void` | 设置优先级 |
| `getCategory()` | 无 | `String` | 获取分类 |
| `setCategory(String category)` | `category` — 分类 | `void` | 设置分类 |
| `getRemindTime()` | 无 | `long` | 获取提醒时间戳 |
| `setRemindTime(long remindTime)` | `remindTime` — 毫秒时间戳 | `void` | 设置提醒时间 |
| `isReminded()` | 无 | `boolean` | 获取是否已提醒 |
| `setReminded(boolean reminded)` | `reminded` — 是否已提醒 | `void` | 设置已提醒状态 |
| `getCreateTime()` | 无 | `Date` | 获取创建时间 |
| `setCreateTime(Date createTime)` | `createTime` — 创建时间 | `void` | 设置创建时间 |
| `toString()` | 无 | `String` | 返回所有字段的字符串表示，用于调试 |

---

### Storage — 数据持久化工具类

> 包：`com.todoapp.storage`  
> 作用：负责 `ArrayList<Todo>` 与 `data/todos.json` 之间的 JSON 序列化与反序列化。全部为静态方法，使用 Gson（美化输出 + UTF-8 编码）。

#### 字段

| 字段名 | 类型 | 访问权限 | 作用 |
|--------|------|----------|------|
| `DATA_FILE` | `String` | private static final | 数据文件路径常量，值为 `"data/todos.json"` |
| `gson` | `Gson` | private static final | Gson 实例，配置了 `setPrettyPrinting()` 美化 JSON 输出 |
| `TODO_LIST_TYPE` | `Type` | private static final | `ArrayList<Todo>` 的类型令牌，用于反序列化泛型 |

#### 公共方法

| 方法名 | 参数列表 | 返回类型 | 作用 |
|--------|----------|----------|------|
| `save(ArrayList<Todo> list)` | `list` — 待保存的 Todo 列表 | `void` | 将列表序列化为 JSON 写入 `data/todos.json`。若 list 为 null 打印警告并返回；自动创建上级目录；IO 异常时打印错误信息 |
| `load()` | 无 | `ArrayList<Todo>` | 从 `data/todos.json` 读取 JSON 并反序列化。文件不存在时返回空列表；JSON 内容为 null 时返回空列表；JSON 格式损坏时打印警告并返回空列表。**永不返回 null** |

#### 异常处理策略

| 情况 | 行为 |
|------|------|
| `save(null)` | 打印 `System.err` 警告，不写入文件 |
| 数据目录不存在 | `save()` 自动调用 `mkdirs()` 创建 |
| 文件不存在 | `load()` 返回空 `ArrayList` |
| JSON 为 null | `load()` 返回空 `ArrayList` |
| JSON 格式损坏 | `load()` 捕获 `JsonSyntaxException`，打印警告，返回空列表 |
| 其他 IOException | 打印 `System.err` 错误信息，`load()` 返回空列表 |

---

### Main — 程序入口

> 包：`com.todoapp`  
> 作用：设置 FlatLaf 苹果风格主题，在 EDT 线程中启动主窗口。

#### 公共方法

| 方法名 | 参数列表 | 返回类型 | 作用 |
|--------|----------|----------|------|
| `main(String[] args)` | `args` — 命令行参数 | `void` | 先调用 `FlatMacLightLaf.setup()` 设置浅色苹果主题，设置全局默认字体，再通过 `SwingUtilities.invokeLater()` 启动 `MainWindow` |

---

### MainWindow — 主窗口

> 包：`com.todoapp.ui`  
> 作用：继承 `JFrame`，展示待办事项表格和操作按钮，是用户交互的核心界面。

#### 字段

| 字段名 | 类型 | 访问权限 | 作用 |
|--------|------|----------|------|
| `todoList` | `ArrayList<Todo>` | private | 内存中的待办事项数据集合 |
| `tableModel` | `TodoTableModel` | private | 自定义表格模型 |
| `table` | `JTable` | private | 待办事项表格视图 |
| `addButton` | `JButton` | private | "添加" 按钮 |
| `editButton` | `JButton` | private | "编辑" 按钮 |
| `deleteButton` | `JButton` | private | "删除" 按钮 |
| `toggleButton` | `JButton` | private | "标记完成/未完成" 按钮 |
| `clearButton` | `JButton` | private | "清理已完成" 按钮 |
| `reminderTimer` | `javax.swing.Timer` | private final | 后台定时扫描器，每 60 秒检查到期提醒，启动后 5 秒首次扫描 |
| `trayIcon` | `TrayIcon` | private | 系统托盘图标，null 表示不支持托盘或初始化失败 |
| `trayEnabled` | `boolean` | private | 托盘是否成功初始化 |
| `sorter` | `TableRowSorter<TodoTableModel>` | private | 表格排序器（字段化以支持动态 RowFilter 筛选） |
| `searchField` | `JTextField` | private final | 搜索标题输入框（18 列），通过 DocumentListener 实时筛选 |
| `statusFilterCombo` | `JComboBox<String>` | private final | 完成状态下拉框：全部 / 未完成 / 已完成 |

#### 构造方法

| 方法名 | 参数列表 | 返回类型 | 作用 |
|--------|----------|----------|------|
| `MainWindow()` | 无 | — | 加载数据、初始化表格（800×600，居中，默认按标题升序排列）、配置 TableRowSorter、构建按钮面板、注册键盘快捷键（Ctrl+N 添加，Delete 删除）、初始化系统托盘（图标+右键菜单）、添加窗口关闭监听（隐藏到托盘而非退出）、启动提醒定时器（间隔 60s，5s 后首次扫描），默认关闭操作为 `DO_NOTHING_ON_CLOSE` |

#### 公共方法（按钮操作与排序支持）

| 方法名 | 参数列表 | 返回类型 | 作用 |
|--------|----------|----------|------|
| `getSelectedModelRow()` | 无 | `int` | 通过 `table.convertRowIndexToModel()` 将视图选中行转为模型索引（兼容表格排序状态），无选中返回 -1 |
| `addTodo()` | 无 | 无（private） | 打开 TodoDialog（添加模式），确认后添加新 Todo，保存并刷新表格，滚动到新增行（已适配排序状态） |
| `editSelected()` | 无 | 无（private） | 通过 `getSelectedModelRow()` 获取模型行 → 打开 TodoDialog（编辑模式，预填原数据），确认后更新 Todo，保存并刷新该行 |
| `deleteSelected()` | 无 | 无（private） | 通过 `getSelectedModelRow()` 获取模型行 → 弹出确认对话框 → 删除 Todo，保存并刷新表格 |
| `toggleSelected()` | 无 | 无（private） | 通过 `getSelectedModelRow()` 获取模型行 → 切换 `completed` 状态，保存并局部刷新该行 |
| `clearCompleted()` | 无 | 无（private） | 统计已完成数量 → 弹出确认对话框（显示数量） → 删除所有 `completed=true` 的项，保存并刷新 |

> 注：以上 5 个方法为 private，但因各按钮 ActionListener 依赖其逻辑，故列出。所有涉及选中行的操作均通过 `getSelectedModelRow()` 转换视图坐标，保证排序后操作正确。

#### 排序规则

| 列 | 列索引 | 排序方式 |
|----|--------|----------|
| 标题 | 0 | 默认字符串排序，启动时按此列升序排列 |
| 优先级 | 1 | 自定义比较器 `高(1) > 中(2) > 低(3)`，未知值排最后 |
| 分类 | 2 | 默认字符串排序 |
| 完成状态 | 3 | `true` 排前（已完成优先展示） |
| 提醒时间 | 4 | 按时间升序：已过期 → 未来时间 → "无" 排最后 |

#### 搜索与筛选

| 方法名 | 参数列表 | 返回类型 | 作用 |
|--------|----------|----------|------|
| `createFilterPanel()` | 无 | `JPanel`（private） | 创建顶部搜索筛选面板：搜索标题文本框 + 状态下拉框 + 清除按钮；绑定 DocumentListener（实时搜索）、ActionListener（状态变化）、清除按钮监听 |
| `applyFilter()` | 无 | 无（private） | 构建 `RowFilter` 列表：标题搜索（`(?i)keyword`，列 0，大小写不敏感+正则转义）+ 完成状态（`^false$`/`^true$`，列 3）；空列表时 `setRowFilter(null)`；单过滤器直接设置；多过滤器 `RowFilter.andFilter()` 组合 |

#### 筛选规则

| 筛选 | 作用列 | 实现方式 |
|------|--------|----------|
| 搜索标题 | 列 0（标题） | `RowFilter.regexFilter("(?i)" + Pattern.quote(keyword), 0)` — 大小写不敏感包含匹配，正则特殊字符自动转义 |
| 状态="全部" | — | 不添加状态筛选，显示所有行 |
| 状态="未完成" | 列 3（Boolean） | `RowFilter.regexFilter("^false$", 3)` |
| 状态="已完成" | 列 3（Boolean） | `RowFilter.regexFilter("^true$", 3)` |
| 组合筛选 | 列 0 + 列 3 | `RowFilter.andFilter(List.of(searchFilter, statusFilter))` — AND 逻辑 |
| 清除 | — | 清空文本框 + 下拉复位"全部" → `applyFilter()` → `sorter.setRowFilter(null)` |

#### 提醒机制

| 方法名 | 参数列表 | 返回类型 | 作用 |
|--------|----------|----------|------|
| `scanReminders()` | 无 | 无（private） | 遍历 `todoList`，找出满足 `remindTime != -1 && remindTime <= now && !reminded` 的 Todo；全部标记 `reminded=true` 并持久化；有到期项时调用 `showReminderNotification()` |
| `showReminderNotification(List<Todo> dueList)` | `dueList` — 到期列表 | 无（private） | 优先使用 `trayIcon.displayMessage()` 发送托盘气泡通知（标题：首条标题 + "等 N 条待办"）；托盘不可用时降级为 `JOptionPane` 对话框 |
| `stopReminderTimer()` | 无 | `void`（public） | 停止后台定时器（窗口隐藏到托盘时调用） |
| `restartReminderTimer()` | 无 | `void`（public） | 重新启动后台定时器（从托盘恢复窗口时调用） |

#### 提醒规则

| 规则 | 说明 |
|------|------|
| 触发条件 | `remindTime != -1 && remindTime <= System.currentTimeMillis() && !reminded` |
| 防重复 | 触发后立即将 `reminded` 设为 `true` 并保存，同一 Todo 只提醒一次 |
| 扫描频率 | `javax.swing.Timer` 每 60 秒触发一次，首次扫描在构造后 5 秒 |
| 运行线程 | Timer 的 ActionListener 在 EDT 执行，无需额外 `SwingUtilities.invokeLater()` |
| 通知方式 | 优先系统托盘气泡，托盘不可用时降级为 `JOptionPane` 对话框 |

#### 系统托盘

| 方法名 | 参数列表 | 返回类型 | 作用 |
|--------|----------|----------|------|
| `setupSystemTray()` | 无 | 无（private） | 检测 `SystemTray.isSupported()` → 调用 `createTrayImage()` 生成 16×16 图标 → 创建 PopupMenu（显示主窗口 / 退出程序） → 注册托盘图标；失败时 `trayIcon` 保持 null |
| `createTrayImage()` | 无 | `Image`（private） | 程序化生成 16×16 托盘图标：蓝色圆角方块 + 白色勾号（BufferedImage + Graphics2D），无需外部图片文件 |
| `hideToTray()` | 无 | 无（private） | 隐藏窗口，通过 `trayIcon.displayMessage()` 提示"程序已最小化到系统托盘" |
| `showMainWindow()` | 无 | 无（private） | 恢复窗口可见、前置、获取焦点 |
| `exitApp()` | 无 | 无（private） | 从 SystemTray 移除图标，调用 `System.exit(0)` |

#### 托盘交互

| 操作 | 行为 |
|------|------|
| 点击窗口关闭按钮 × | 触发 `hideToTray()`：隐藏窗口 + 托盘气泡提示 |
| 双击托盘图标 | 触发 `showMainWindow()`：恢复窗口 |
| 右键托盘图标 → "显示主窗口" | 恢复窗口 |
| 右键托盘图标 → "退出程序" | 清理托盘 + `System.exit(0)` |

---

### TodoTableModel — 表格模型

> 包：`com.todoapp.ui`  
> 作用：继承 `AbstractTableModel`，将 `ArrayList<Todo>` 映射为 5 列表格视图。第 4 列（完成状态）渲染为复选框并可直接编辑。

#### 字段

| 字段名 | 类型 | 访问权限 | 作用 |
|--------|------|----------|------|
| `COLUMN_NAMES` | `String[]` | private static final | 列名：`{"标题", "优先级", "分类", "完成状态", "提醒时间"}` |
| `DATE_FORMAT` | `SimpleDateFormat` | private static final | 提醒时间的格式化器，格式 `"yyyy-MM-dd HH:mm"` |
| `todoList` | `ArrayList<Todo>` | private final | 数据源引用（与 MainWindow 共享同一个列表） |

#### 构造方法

| 方法名 | 参数列表 | 返回类型 | 作用 |
|--------|----------|----------|------|
| `TodoTableModel(ArrayList<Todo> todoList)` | `todoList` — 数据列表 | — | 绑定数据源 |

#### 公共方法

| 方法名 | 参数列表 | 返回类型 | 作用 |
|--------|----------|----------|------|
| `getRowCount()` | 无 | `int` | 返回 `todoList.size()` |
| `getColumnCount()` | 无 | `int` | 返回 5 |
| `getColumnName(int column)` | `column` — 列索引 | `String` | 返回对应列名 |
| `getColumnClass(int columnIndex)` | `columnIndex` — 列索引 | `Class<?>` | 第 3 列返回 `Boolean.class`（渲染复选框），其余返回 `String.class` |
| `isCellEditable(int rowIndex, int columnIndex)` | `rowIndex` — 行, `columnIndex` — 列 | `boolean` | 仅第 3 列（完成状态）可编辑 |
| `getValueAt(int rowIndex, int columnIndex)` | `rowIndex` — 行, `columnIndex` — 列 | `Object` | 根据行列返回对应字段值；提醒时间列返回格式化字符串（-1 → "无"，已过期 → 追加" (已过期)"） |
| `setValueAt(Object aValue, int rowIndex, int columnIndex)` | `aValue` — 新值, `rowIndex` — 行, `columnIndex` — 列 | `void` | 仅处理第 3 列的 Boolean 切换，调用 `fireTableCellUpdated` |
| `refresh()` | 无 | `void` | 调用 `fireTableDataChanged()` 全量刷新 |
| `updateRow(int row)` | `row` — 行索引 | `void` | 调用 `fireTableRowsUpdated(row, row)` 局部刷新 |

---

### TodoDialog — 添加/编辑对话框

> 包：`com.todoapp.ui`  
> 作用：继承 `JDialog`，模态对话框，提供标题、优先级、分类（含"新分类"按钮可动态添加自定义分类）、提醒时间四个输入项。用于添加和编辑 Todo。

#### 字段

| 字段名 | 类型 | 访问权限 | 作用 |
|--------|------|----------|------|
| `titleField` | `JTextField` | private final | 标题输入框（20 列） |
| `priorityCombo` | `JComboBox<String>` | private final | 优先级下拉框（高/中/低） |
| `categoryCombo` | `JComboBox<String>` | private final | 分类下拉框（可编辑：工作/个人/购物/学习/健康），旁有"新分类"按钮可动态添加自定义分类 |
| `reminderCheck` | `JCheckBox` | private final | "设置提醒" 复选框 |
| `dateSpinner` | `JSpinner` | private final | 日期时间选择器（SpinnerDateModel + "yyyy-MM-dd HH:mm" 编辑器） |
| `confirmed` | `boolean` | private | 对话框返回结果：true = 用户点击确定 |

#### 构造方法

| 方法名 | 参数列表 | 返回类型 | 作用 |
|--------|----------|----------|------|
| `TodoDialog(Frame owner)` | `owner` — 父窗口 | — | 添加模式，标题为"添加待办事项"，默认提醒时间为明天此时，默认不启用提醒 |
| `TodoDialog(Frame owner, Todo todo)` | `owner` — 父窗口, `todo` — 原 Todo | — | 编辑模式，标题为"编辑待办事项"，预填原 Todo 的所有字段值 |

#### 公共方法

| 方法名 | 参数列表 | 返回类型 | 作用 |
|--------|----------|----------|------|
| `isConfirmed()` | 无 | `boolean` | 返回用户是否点击了"确定"（标题为空时拒绝关闭） |
| `fillTodo(Todo todo)` | `todo` — 要填充的 Todo | `void` | 将对话框中的当前输入值写入已有 Todo（编辑模式使用） |
| `createTodo()` | 无 | `Todo` | 根据对话框中输入值创建并返回新 Todo（添加模式使用） |
| `addNewCategory()` | 无 | 无（private） | 弹出 `JOptionPane.showInputDialog` 输入新分类名，去重后添加到 `categoryCombo` 并选中；取消或空输入不操作 |

---

## 部署

### 构建步骤

```bash
# 1. 编译所有源文件
javac -encoding UTF-8 -cp "lib/gson-2.14.0.jar;lib/flatlaf-3.7.1.jar" -d out \
    src/com/todoapp/Main.java \
    src/com/todoapp/model/Todo.java \
    src/com/todoapp/storage/Storage.java \
    src/com/todoapp/ui/TodoTableModel.java \
    src/com/todoapp/ui/TodoDialog.java \
    src/com/todoapp/ui/MainWindow.java

# 2. 打包 JAR（MANIFEST.MF 已创建）
jar cfm TodoApp.jar MANIFEST.MF -C out .

# 3. 运行
java -jar TodoApp.jar
# 或直接双击 run.bat
```

### MANIFEST.MF

```mf
Manifest-Version: 1.0
Main-Class: com.todoapp.Main
Class-Path: lib/gson-2.14.0.jar lib/flatlaf-3.7.1.jar
```

### 启动脚本 (run.bat)

```bat
@echo off
title TodoApp
cd /d "%~dp0"
java -jar TodoApp.jar
pause
```

此脚本确保从任何位置启动时，工作目录都指向批处理文件所在目录（即项目根目录），从而正确加载 `data/` 和 `lib/` 下的文件。

### 桌面快捷方式

1. 右键 `run.bat` → **发送到** → **桌面快捷方式**
2. （可选）右键桌面快捷方式 → **属性** → **更改图标** → 选择一个 `.ico` 文件

---

## 原生打包（jpackage）

### 概述

使用 JDK 14+ 自带的 `jpackage` 工具创建原生应用包，可**捆绑 JRE**，用户无需安装 Java。

### 输出类型

| 类型 | 命令 | 是否需要 WiX | 产物 |
|------|------|-------------|------|
| `app-image` | `jpackage --type app-image` | ❌ 不需要 | 自包含文件夹（含 EXE + JRE），可直接分发 |
| `exe` | `jpackage --type exe` | ✅ 需要 | EXE 安装程序 |
| `msi` | `jpackage --type msi` | ✅ 需要 | MSI 安装程序（可通过组策略部署） |

### app-image 构建（推荐 — 无需额外工具）

```bash
# 1. 编译 + 打包 JAR（同阶段 8）
javac -encoding UTF-8 -cp "lib/*" -d out src/com/todoapp/**/*.java
jar cfm TodoApp.jar MANIFEST.MF -C out .

# 2. 准备输入目录
mkdir -p build/input/lib build/input/data
cp TodoApp.jar build/input/
cp lib/*.jar build/input/lib/

# 3. jpackage 创建自包含应用镜像
jpackage \
    --name "TodoApp" \
    --input build/input \
    --main-jar TodoApp.jar \
    --main-class com.todoapp.Main \
    --type app-image \
    --dest dist \
    --app-version "1.0" \
    --description "TodoApp - 待办事项管理应用" \
    --vendor "TodoApp"
```

### 构建结果

```
dist/TodoApp/
├── TodoApp.exe          ← 双击启动，捆绑 JRE 无需用户安装 Java
├── app/
│   ├── TodoApp.jar
│   ├── lib/             ← Gson + FlatLaf
│   └── data/            ← 运行时数据目录
├── runtime/             ← 捆绑的 JRE（约 120 MB）
└── *.dll                ← 系统库文件
```

### 一键构建脚本 (build.bat)

```bat
build.bat    # 自动完成：清理 → 编译 → 打包 JAR → jpackage app-image
```

### EXE/MSI 安装程序（需要 WiX Toolset）

```bash
# 1. 下载安装 WiX Toolset 3.x：https://wixtoolset.org/
# 2. 将 WiX bin 目录添加到系统 PATH
# 3. 运行 jpackage --type exe（或 --type msi）

jpackage \
    --name "TodoApp" \
    --input build/input \
    --main-jar TodoApp.jar \
    --main-class com.todoapp.Main \
    --type exe \
    --dest dist \
    --app-version "1.0" \
    --description "TodoApp - 待办事项管理应用" \
    --vendor "TodoApp" \
    --win-shortcut \
    --win-menu \
    --win-menu-group "TodoApp"
```

### 验证结果

```
运行 dist/TodoApp/TodoApp.exe：
  → [Storage] 已加载 0 条记录
  → [Tray] 系统托盘已就绪
  ✅ 无需安装 Java，开箱即用
```

---

<!-- stage 9 completed -->
