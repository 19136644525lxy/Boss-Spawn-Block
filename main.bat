@echo off
chcp 65001 >nul
title Git 常驻监控提交工具
mode con cols=90 lines=30
setlocal enabledelayedexpansion

:MAIN
cls
echo.
echo ==============================================
echo          Git 常驻监控提交工具
echo ==============================================
echo.
echo 当前分支：
git branch --show-current
echo.
echo 状态：正在监控项目文件变化...
echo 每10秒检查一次，按 Ctrl+C 可退出
echo ==============================================
echo.

:: 检查是否有修改
git status --porcelain > temp.txt
for %%i in (temp.txt) do if %%~zi gtr 0 (
    del temp.txt
    goto ALERT
)
del temp.txt

echo [%time%] 未检测到修改，继续监控...
timeout /t 10 /nobreak >nul
goto MAIN

:ALERT
echo.
echo ==============================================
echo 检测到项目文件已修改！
echo ==============================================
echo.
echo 是否立即提交并推送到 GitHub？
echo 请输入 Y 提交推送 / N 忽略本次
echo.
set /p "confirm=请选择(Y/N)："

if /i "!confirm!"=="Y" goto COMMIT
if /i "!confirm!"=="N" (
    echo 已忽略，3秒后返回监控界面...
    timeout /t 3 /nobreak >nul
    goto MAIN
)
echo 无效输入，已忽略，3秒后返回监控界面...
timeout /t 3 /nobreak >nul
goto MAIN

:COMMIT
echo.
set /p "msg=请输入提交说明（直接回车默认：自动监控提交）："
if "!msg!"=="" set "msg=自动监控提交"

echo.
echo 正在添加所有修改文件...
git add .

echo 正在提交：!msg!
git commit -m "!msg!"

echo 正在推送到 GitHub...
git push origin HEAD

echo.
echo 提交推送完成！3秒后返回监控界面...
timeout /t 3 /nobreak >nul
goto MAIN