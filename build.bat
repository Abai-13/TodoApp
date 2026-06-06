@echo off
REM ============================================================
REM  TodoApp Build Script (Windows)
REM  使用 jpackage 创建自包含应用镜像（捆绑 JRE）
REM
REM  前置条件：
REM    - JDK 17+ (含 jpackage)
REM    - 已编译的 TodoApp.jar（运行 build-jar 或用 javac 编译）
REM
REM  可选生成 MSI/EXE 安装程序：
REM    - 安装 WiX Toolset 3.x (https://wixtoolset.org/)
REM    - 将 WiX 的 bin 目录添加到 PATH
REM    - 取消下面 jpackage-exe 节的注释
REM ============================================================
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo [1/4] 清理旧的构建产物...
if exist build rmdir /s /q build
if exist dist  rmdir /s /q dist

echo [2/4] 编译 Java 源文件...
mkdir build\classes
javac -encoding UTF-8 -cp "lib\gson-2.14.0.jar;lib\flatlaf-3.7.1.jar" -d build\classes ^
    src\com\todoapp\Main.java ^
    src\com\todoapp\model\Todo.java ^
    src\com\todoapp\storage\Storage.java ^
    src\com\todoapp\ui\TodoTableModel.java ^
    src\com\todoapp\ui\TodoDialog.java ^
    src\com\todoapp\ui\MainWindow.java
if errorlevel 1 (
    echo 编译失败！
    exit /b 1
)

echo [3/4] 打包 JAR...
jar cfm build\TodoApp.jar MANIFEST.MF -C build\classes .
mkdir build\input\lib
mkdir build\input\data
copy build\TodoApp.jar build\input\ >nul
copy lib\gson-2.14.0.jar build\input\lib\ >nul
copy lib\flatlaf-3.7.1.jar build\input\lib\ >nul

echo [4/4] 使用 jpackage 创建应用镜像...
jpackage ^
    --name "TodoApp" ^
    --input build\input ^
    --main-jar TodoApp.jar ^
    --main-class com.todoapp.Main ^
    --type app-image ^
    --dest dist ^
    --app-version "1.0" ^
    --description "TodoApp - 待办事项管理应用" ^
    --vendor "TodoApp"

if errorlevel 1 (
    echo jpackage 失败！
    exit /b 1
)

echo.
echo ================================================================
echo  构建成功！
echo  输出: dist\TodoApp\TodoApp.exe
echo  可直接运行，无需安装 Java
echo.
echo  如需创建 EXE 安装程序，请安装 WiX Toolset 后取消下方注释：
echo ================================================================
goto :eof

REM ==== 可选：jpackage EXE 安装程序（需要 WiX Toolset 3.x）====
:jpackage-exe
jpackage ^
    --name "TodoApp" ^
    --input build\input ^
    --main-jar TodoApp.jar ^
    --main-class com.todoapp.Main ^
    --type exe ^
    --dest dist ^
    --app-version "1.0" ^
    --description "TodoApp - 待办事项管理应用" ^
    --vendor "TodoApp" ^
    --win-shortcut ^
    --win-menu ^
    --win-menu-group "TodoApp"
