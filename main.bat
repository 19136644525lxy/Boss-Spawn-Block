@echo off
chcp 65001 >nul
cls
color 0A
echo ==============================================
echo          Git 提交 + 推送 GitHub 脚本
echo ==============================================
echo.

:: 输入提交信息
set /p commit_msg=请输入本次更新内容：

:: 如果没输入，给默认值
if "%commit_msg%"=="" (
    set commit_msg=快速更新
)

echo.
echo [1/3] 正在添加所有文件...
git add .

echo.
echo [2/3] 正在本地提交：%commit_msg%
git commit -m "%commit_msg%"

echo.
echo [3/3] 正在推送到 GitHub 远程仓库...
git push origin HEAD

echo.
echo ==============================================
echo              ✅ 推送完成！
echo ==============================================
echo.
pause