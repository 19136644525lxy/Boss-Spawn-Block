@echo off
chcp 65001 >nul
echo 正在同步代码到GitHub...

:: 添加所有更改
git add .
if %errorlevel% neq 0 (
    echo 添加文件失败
    pause
    exit /b 1
)

:: 提交更改
set /p commit_message=请输入提交信息: 
git commit -m "%commit_message%"
if %errorlevel% neq 0 (
    echo 提交失败
    pause
    exit /b 1
)

:: 推送到GitHub
git push
if %errorlevel% neq 0 (
    echo 推送失败
    pause
    exit /b 1
)

echo 代码同步成功！
pause