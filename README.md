# TodoApp — 待办事项管理应用

基于 Java Swing 的桌面待办事项管理工具，支持优先级/分类管理、搜索筛选、到期提醒和系统托盘。

## ✨ 功能

- **增删改查** — 添加、编辑、删除、标记完成/未完成，批量清理已完成
- **优先级** — 高 / 中 / 低 三级，表格自定义排序
- **分类** — 内置 5 种（工作/个人/购物/学习/健康），支持动态添加自定义分类
- **搜索筛选** — 标题实时搜索 + 完成状态筛选，AND 组合
- **表格排序** — 按标题、优先级、分类、状态、提醒时间排序，点击列头即可
- **到期提醒** — 设置提醒时间，后台定时扫描（60s），系统托盘气泡通知
- **系统托盘** — 关闭窗口最小化到托盘，双击恢复，右键退出
- **键盘快捷键** — `Ctrl+N` 添加，`Delete` 删除
- **数据持久化** — JSON 文件存储，每次操作自动保存

## 🛠 技术栈

| 技术 | 说明 |
|------|------|
| Java 17+ | 开发语言 |
| Swing | GUI 框架 |
| [FlatLaf](https://www.formdev.com/flatlaf/) 3.7.1 | 苹果风格 Look & Feel |
| [Gson](https://github.com/google/gson) 2.14.0 | JSON 序列化/反序列化 |
| jpackage | 打包为 Windows 自包含 EXE |

## 📁 项目结构

```
TodoApp/
├── src/com/todoapp/
│   ├── Main.java                 # 程序入口，主题初始化
│   ├── model/Todo.java           # 数据模型
│   ├── storage/Storage.java      # JSON 持久化
│   └── ui/
│       ├── MainWindow.java       # 主窗口（表格、搜索筛选、托盘、提醒）
│       ├── TodoDialog.java       # 添加/编辑对话框
│       └── TodoTableModel.java   # 表格模型
├── lib/                          # 第三方依赖
│   ├── gson-2.14.0.jar
│   └── flatlaf-3.7.1.jar
├── data/todos.json               # 数据文件（运行时生成）
├── docs/API.md                   # API 文档
├── MANIFEST.MF                   # JAR 清单
├── build.bat                     # jpackage 构建脚本
└── run.bat                       # 一键运行脚本
```

## 🚀 快速开始

### 环境要求

- JDK 17 或更高版本

### 运行

```bash
# 克隆仓库
git clone https://github.com/Abai-13/TodoApp.git
cd TodoApp

# 方式一：直接运行 JAR
java -jar TodoApp.jar

# 方式二：双击 run.bat（Windows）
```

### 从源码编译运行

```bash
# 编译
javac -encoding UTF-8 -cp "lib/*" -d out \
    src/com/todoapp/Main.java \
    src/com/todoapp/model/Todo.java \
    src/com/todoapp/storage/Storage.java \
    src/com/todoapp/ui/TodoTableModel.java \
    src/com/todoapp/ui/TodoDialog.java \
    src/com/todoapp/ui/MainWindow.java

# 打包 JAR
jar cfm TodoApp.jar MANIFEST.MF -C out .

# 运行
java -jar TodoApp.jar
```

## 📦 打包为 Windows EXE

无需安装 Java 即可运行：

```bash
# 一键构建（需要 JDK 17+ 含 jpackage）
build.bat
```

输出：`dist/TodoApp/TodoApp.exe` — 双击运行，捆绑 JRE，开箱即用。

> 如需生成 MSI/EXE 安装程序，需安装 [WiX Toolset 3.x](https://wixtoolset.org/)。

## 📖 文档

- [API 文档](docs/API.md) — 类、字段、方法详细说明

## 📝 使用指南

### 添加待办

1. 点击 **添加** 按钮或按 `Ctrl+N`
2. 输入标题（必填）、选择优先级和分类
3. 可选：勾选"设置提醒"并选择日期时间
4. 点击 **确定**

### 编辑/删除

- 选中一行 → 点击 **编辑** 或 **删除**（`Delete` 键）
- 双击行也可触发编辑

### 搜索筛选

- 顶部输入框输入关键词实时搜索标题
- 状态下拉框筛选"未完成"/"已完成"
- 点击 **清除筛选** 重置

### 提醒通知

- 到期提醒以系统托盘气泡弹出
- 托盘不可用时降级为对话框通知
- 每条待办只提醒一次

## 📄 许可

MIT License
