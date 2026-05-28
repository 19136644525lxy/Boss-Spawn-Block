@echo off
chcp 65001 >nul
title Git 常驻监控提交工具
mode con cols=90 lines=30

:LOOP
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
echo （每10秒检查一次，按 Ctrl+C 可退出）
echo ==============================================
echo.

:: 检查是否有修改
git status --porcelain > temp.txt
for %%i in (temp.txt) do if %%~zi gtr 0 (
    del temp.txt
    goto FOUND_CHANGES
)
del temp.txt

echo [%time%] 未检测到修改，继续监控...
timeout /t 10 /nobreak >nul
goto LOOP

:FOUND_CHANGES
cls
echo.
echo ==============================================
echo ⚠️  检测到项目文件已修改！
echo ==============================================
echo.
echo 是否立即提交并推送到 GitHub？
echo.
echo [Y] 提交并推送   [N] 忽略本次，继续监控
echo.
set /p "confirm=请选择(Y/N)："

if /i "%confirm%"=="Y" goto DO_COMMIT
if /i "%confirm%"=="N" (
    echo 已忽略本次修改，继续监控...
    timeout /t 2 /nobreak >nul
    goto LOOP
)
:: 输入无效，直接忽略
echo 无效输入，已忽略本次修改...
timeout /t 2 /nobreak >nul
goto LOOP

:DO_COMMIT
echo.
set /p "commit_msg=请输入提交说明（直接回车默认：自动监控提交）："
if "%commit_msg%"=="" set "commit_msg=自动监控提交"

echo.
echo [1/3] 添加所有修改文件...
git add .

echo [2/3] 本地提交：%commit_msg%
git commit -m "%commit_msg%"

echo [3/3] 推送到 GitHub 远程仓库...
git push origin HEAD

echo.
echo ✅ 提交并推送完成！
echo.
pause
goto LOOP