@echo off
chcp 65001 >nul
title Git 自动监控 + 提交推送工具
mode con cols=90 lines=30

:MAIN
cls
echo.
echo ==============================================
echo          Git 自动监控 + 提交推送工具
echo ==============================================
echo.
echo 当前分支：
git branch --show-current
echo.
echo -------------------- 功能菜单 --------------------
echo.
echo [1] 手动输入信息 提交并推送 GitHub
echo [2] 进入自动监控模式（每10秒检查更新）
echo [3] 退出程序
echo.
set /p "choice=请选择功能(1/2/3)："

if "%choice%"=="1" goto COMMIT
if "%choice%"=="2" goto AUTO_MONITOR
if "%choice%"=="3" exit
goto MAIN

:: ==============================================
:: 手动提交模式
:: ==============================================
:COMMIT
cls
echo.
echo ==================== 手动提交 ====================
echo.
set /p "msg=请输入提交说明："
if "%msg%"=="" set "msg=快速更新"

echo.
echo [1/3] 添加所有文件...
git add .

echo [2/3] 本地提交中...
git commit -m "%msg%"

echo [3/3] 推送到 GitHub...
git push origin HEAD

echo.
echo 提交并推送完成！
echo.
pause
goto MAIN

:: ==============================================
:: 自动监控模式（10秒检查一次）
:: ==============================================
:AUTO_MONITOR
cls
echo.
echo ==================================================
echo          已进入自动监控模式
echo          每10秒检查一次项目修改
echo          检测到修改会提示你提交
echo ==================================================
echo.
echo 按 Ctrl + C 可退出监控，返回主菜单
echo.

:LOOP
git status --porcelain > temp.txt
for %%i in (temp.txt) do if %%~zi gtr 0 (
    del temp.txt
    goto FOUND_CHANGES
)
del temp.txt

echo [%time%] 未检测到修改，10秒后重新检查...
timeout /t 10 /nobreak >nul
goto LOOP

:FOUND_CHANGES
cls
echo ==================================================
echo 检测到项目文件已修改！
echo ==================================================
echo.
echo 是否立即提交并推送到 GitHub？
echo.
echo [Y] 是   [N] 忽略本次，继续监控
echo.
set /p "confirm=请选择(Y/N)："
if /i "%confirm%"=="Y" goto AUTO_COMMIT
if /i "%confirm%"=="N" (
    echo 已忽略，继续监控...
    timeout /t 2 /nobreak >nul
    goto LOOP
)
goto LOOP

:AUTO_COMMIT
echo.
set /p "auto_msg=输入提交说明（默认：自动监控提交）："
if "%auto_msg%"=="" set "auto_msg=自动监控提交"

echo.
git add .
git commit -m "%auto_msg%"
git push origin HEAD

echo.
echo 自动提交推送完成！
timeout /t 2 /nobreak >nul
goto LOOP