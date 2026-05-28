@echo off
chcp 65001 >nul
cls
echo ==============================================
echo          Git 一键提交更新脚本
echo ==============================================
echo.

:: 1. 让你输入提交说明
set /p msg=请输入本次更新的说明（必填）：

:: 如果没输入内容，自动给一个默认提交信息
if not defined msg (
    set msg="自动提交：快速更新"
)

echo.
echo 正在提交代码，请稍候...
echo.

:: 2. Git 核心命令
git add .
git commit -m "%msg%"
git push

echo.
echo ==============================================
echo          提交完成！已推送到 GitHub
echo ==============================================
echo.

:: 暂停窗口，方便看结果
pause