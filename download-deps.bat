@echo off
setlocal enabledelayedexpansion

REM Get the project directory
set "PROJ=%~dp0"
if "%PROJ:~-1%"=="\" set "PROJ=%PROJ:~0,-1%"

set "LIBS=%PROJ%\libs"
mkdir "!LIBS!" 2>nul

echo ^>^> downloading dependencies...
echo.

REM Maven Central base URL
set "MAVEN=https://repo1.maven.org/maven2"
set "FABRIC=https://maven.fabricmc.net"
set "TERRAFORMERS=https://maven.terraformersmc.com/releases"

REM Dependencies to download
REM Format: URL/path/to/jar FileName

set "DEPS[0]=!MAVEN!/com/google/code/gson/gson/2.11.0/gson-2.11.0.jar gson-2.11.0.jar"
set "DEPS[1]=!MAVEN!/com/mojang/brigadier/1.0.18/brigadier-1.0.18.jar brigadier-1.0.18.jar"
set "DEPS[2]=!MAVEN!/org/jspecify/jspecify/0.3.0/jspecify-0.3.0.jar jspecify-0.3.0.jar"
set "DEPS[3]=!MAVEN!/com/llamalad7/mixinextras/0.4.1/mixinextras-0.4.1.jar mixinextras-0.4.1.jar"
set "DEPS[4]=!MAVEN!/org/lwjgl/lwjgl/3.3.1/lwjgl-3.3.1.jar lwjgl-3.3.1.jar"
set "DEPS[5]=!MAVEN!/org/lwjgl/lwjgl-glfw/3.3.1/lwjgl-glfw-3.3.1.jar lwjgl-glfw-3.3.1.jar"
set "DEPS[6]=!FABRIC!/net/fabricmc/fabric-loader/0.19.3/fabric-loader-0.19.3.jar fabric-loader-0.19.3.jar"
set "DEPS[7]=!FABRIC!/net/fabricmc/fabric-api/fabric-api/0.151.0+26.1.2/fabric-api-0.151.0+26.1.2.jar fabric-api-0.151.0+26.1.2.jar"
set "DEPS[8]=!TERRAFORMERS!/com/terraformersmc/modmenu/15.0.0/modmenu-15.0.0.jar modmenu-15.0.0.jar"
set "DEPS[9]=!MAVEN!/org/spongepowered/mixin/0.8.7/mixin-0.8.7.jar mixin-0.8.7.jar"

set "DEP_COUNT=0"
for /L %%i in (0,1,9) do (
    if defined DEPS[%%i] (
        set /a DEP_COUNT+=1
    )
)

echo Found !DEP_COUNT! dependencies to download
echo.

REM Download each dependency
set "FAILED=0"
for /L %%i in (0,1,9) do (
    if defined DEPS[%%i] (
        for /f "tokens=1,2" %%A in ("!DEPS[%%i]!") do (
            set "URL=%%A"
            set "FILE=%%B"
            
            if exist "!LIBS!\!FILE!" (
                echo [SKIP] !FILE! ^(already exists^)
            ) else (
                echo [DL] !FILE!...
                powershell -Command "(New-Object System.Net.ServicePointManager).SecurityProtocol=[System.Net.SecurityProtocolType]::Tls12; (New-Object System.Net.WebClient).DownloadFile('!URL!', '!LIBS!\!FILE!')" >nul 2>&1
                if !errorlevel! equ 0 (
                    echo [OK] !FILE!
                ) else (
                    echo [FAIL] !FILE! - could not download
                    set /a FAILED+=1
                )
            )
        )
    )
)

echo.
if !FAILED! gtr 0 (
    echo WARNING: !FAILED! dependencies failed to download
    echo Please check your internet connection and try again
    echo.
    pause
)

echo ^>^> dependencies ready in !LIBS!
echo.
pause
