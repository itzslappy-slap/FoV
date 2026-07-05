@echo off
setlocal enabledelayedexpansion

REM Get the project directory
set "PROJ=%~dp0"
if "%PROJ:~-1%"=="\" set "PROJ=%PROJ:~0,-1%"

set "MOD_ID=infinizoom"
if not defined MOD_VERSION set "MOD_VERSION=1.0.2"

REM Find Java
set "JDK=%JAVA_HOME%"
if not defined JDK (
    where javac >nul 2>&1
    if !errorlevel! equ 0 (
        for /f "delims=" %%A in ('where javac') do set "JAVAC_PATH=%%A"
        for %%A in ("!JAVAC_PATH!") do set "JAVAC_DIR=%%~dpA"
        for %%A in ("!JAVAC_DIR!..") do set "JDK=%%~dpA"
        if "!JDK:~-1!"=="\" set "JDK=!JDK:~0,-1!"
    ) else (
        echo error: no JDK found. Set JAVA_HOME ^(Java 25+^) or put javac on PATH. >&2
        exit /b 1
    )
)

REM Check for libs directory
if not defined INFINIZOOM_LIBS set "INFINIZOOM_LIBS=%PROJ%\libs"
if not exist "!INFINIZOOM_LIBS!" (
    echo error: dependency directory not found: !INFINIZOOM_LIBS! >&2
    echo. >&2
    echo This is a manual build for Minecraft 26.1.2, which current Loom cannot build. >&2
    echo It compiles directly against the ^(deobfuscated^) Minecraft client.jar plus the >&2
    echo Fabric runtime jars. Place all of these .jar files in a single directory: >&2
    echo. >&2
    echo   - the deobfuscated Minecraft 26.1.2 client.jar ^(Mojang official mappings^) >&2
    echo   - fabric-loader >&2
    echo   - sponge-mixin >&2
    echo   - fabric-api modules >&2
    echo   - modmenu ^(optional^) >&2
    echo. >&2
    echo Then point the build at it: >&2
    echo. >&2
    echo   set INFINIZOOM_LIBS=C:\path\to\libs >&2
    echo   build-manual.bat >&2
    echo. >&2
    echo Or drop the jars in .\libs next to this script. For normal setups, prefer the >&2
    echo Gradle/Loom build ^(gradlew build^) once Loom supports your version. >&2
    exit /b 1
)

echo Libs directory: !INFINIZOOM_LIBS!

REM Set output directories
set "OUT=%PROJ%\build\manual\classes"
set "DIST=%PROJ%\build\manual\libs"
if exist "!OUT!" rmdir /s /q "!OUT!"
if exist "!DIST!" rmdir /s /q "!DIST!"
mkdir "!OUT!" "!DIST!"

REM Build classpath from all jars
setlocal enabledelayedexpansion
set "CP="
set "JAR_COUNT=0"
for %%F in ("!INFINIZOOM_LIBS!\*.jar") do (
    set /a JAR_COUNT+=1
    if !JAR_COUNT! gtr 1 (
        set "CP=!CP!;%%F"
    ) else (
        set "CP=%%F"
    )
    echo Found: %%~nxF
)

if %JAR_COUNT% equ 0 (
    echo error: no .jar files found in !INFINIZOOM_LIBS! >&2
    exit /b 1
)

echo Classpath count: %JAR_COUNT% jars

REM Compile Java files
echo ^>^> compiling...
set "JAVA_FILES="
for /r "%PROJ%\src\main\java" %%F in (*.java) do (
    set "JAVA_FILES=!JAVA_FILES! "%%F""
)

if "!JAVA_FILES!"=="" (
    echo error: no .java files found >&2
    exit /b 1
)

"!JDK!\bin\javac.exe" --release 25 -cp "!CP!" -d "!OUT!" !JAVA_FILES!
if !errorlevel! neq 0 (
    echo Compilation failed with error code !errorlevel!
    exit /b !errorlevel!
)

REM Package resources
echo ^>^> packaging resources...
if exist "%PROJ%\src\main\resources" (
    xcopy /s /i /y "%PROJ%\src\main\resources\*" "!OUT!" >nul
)

REM Update fabric.mod.json version using Python
echo ^>^> updating fabric.mod.json...
if exist "!OUT!\fabric.mod.json" (
    python -c "import json; d=json.load(open('!OUT!\fabric.mod.json')); d['version']='%MOD_VERSION%'; d.pop('icon',None); json.dump(d,open('!OUT!\fabric.mod.json','w'),indent=2)" 2>nul
    if !errorlevel! neq 0 (
        echo warning: failed to update fabric.mod.json version >&2
    )
)

REM Build jar
set "JAR=!DIST!\!MOD_ID!-!MOD_VERSION!.jar"
echo ^>^> building jar...
pushd "!OUT!"
"!JDK!\bin\jar.exe" --create --file "!JAR!" -C . .
popd
if !errorlevel! neq 0 (
    echo Jar creation failed with error code !errorlevel!
    exit /b !errorlevel!
)

echo ^>^> done: !JAR!

REM Install to mods directory if specified
if defined INFINIZOOM_MODS_DIR (
    echo ^>^> installing to !INFINIZOOM_MODS_DIR!
    if not exist "!INFINIZOOM_MODS_DIR!" mkdir "!INFINIZOOM_MODS_DIR!"
    copy /y "!JAR!" "!INFINIZOOM_MODS_DIR!"
)

endlocal
pause
exit /b 0
