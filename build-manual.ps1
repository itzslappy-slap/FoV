#!/usr/bin/env pwsh
param()

$ErrorActionPreference = "Stop"

# Get the project directory
$PROJ = Split-Path -Parent $MyInvocation.MyCommand.Path

$MOD_ID = "infinizoom"
$MOD_VERSION = if ($env:MOD_VERSION) { $env:MOD_VERSION } else { "1.0.2" }

# Find Java
$JDK = $env:JAVA_HOME
if (-not $JDK) {
    $javac = Get-Command javac -ErrorAction SilentlyContinue
    if ($javac) {
        $JDK = Split-Path -Parent (Split-Path -Parent $javac.Source)
    } else {
        Write-Error "error: no JDK found. Set JAVA_HOME (Java 25+) or put javac on PATH."
        exit 1
    }
}

Write-Host "JDK: $JDK"

# Check for libs directory
$LIBS = if ($env:INFINIZOOM_LIBS) { $env:INFINIZOOM_LIBS } else { "$PROJ\libs" }
if (-not (Test-Path $LIBS)) {
    Write-Error @"
error: dependency directory not found: $LIBS

This is a manual build for Minecraft 26.1.2, which current Loom cannot build.
It compiles directly against the (deobfuscated) Minecraft client.jar plus the
Fabric runtime jars. Place all of these .jar files in a single directory:

  - the deobfuscated Minecraft 26.1.2 client.jar (Mojang official mappings)
  - fabric-loader
  - sponge-mixin
  - fabric-api modules
  - modmenu (optional)

Then point the build at it:

  `$env:INFINIZOOM_LIBS='C:\path\to\libs'; & '.\build-manual.ps1'`

Or drop the jars in .\libs next to this script.
"@
    exit 1
}

Write-Host "Libs directory: $LIBS"

# Set output directories
$OUT = "$PROJ\build\manual\classes"
$DIST = "$PROJ\build\manual\libs"
Write-Host "Output directory: $OUT"

if (Test-Path $OUT) {
    Remove-Item -Recurse -Force $OUT
}
if (Test-Path $DIST) {
    Remove-Item -Recurse -Force $DIST
}
New-Item -ItemType Directory -Path $OUT -Force | Out-Null
New-Item -ItemType Directory -Path $DIST -Force | Out-Null

# Build classpath from all jars
Write-Host ">> building classpath..."
$jars = @(Get-ChildItem -Path $LIBS -Filter "*.jar" -File)
if ($jars.Count -eq 0) {
    Write-Error "error: no .jar files found in $LIBS"
    exit 1
}

$CP = ($jars | ForEach-Object { $_.FullName }) -join ";"
foreach ($jar in $jars) {
    Write-Host "Found: $($jar.Name)"
}
Write-Host "Classpath count: $($jars.Count) jars"
Write-Host ""

# Find all Java files
Write-Host ">> finding Java files..."
$javaFiles = @(Get-ChildItem -Recurse -Path "$PROJ\src\main\java" -Filter "*.java" -File)
if ($javaFiles.Count -eq 0) {
    Write-Error "error: no .java files found"
    exit 1
}
Write-Host "Found $($javaFiles.Count) Java files"
Write-Host ""

# Compile Java files
Write-Host ">> compiling..."
$javacExe = "$JDK\bin\javac.exe"
$javaFilePaths = $javaFiles | ForEach-Object { $_.FullName }

& $javacExe --release 25 -cp $CP -d $OUT -encoding UTF-8 $javaFilePaths
if ($LASTEXITCODE -ne 0) {
    Write-Error "Compilation failed with error code $LASTEXITCODE"
    exit $LASTEXITCODE
}

# Package resources
Write-Host ">> packaging resources..."
$resourcesPath = "$PROJ\src\main\resources"
if (Test-Path $resourcesPath) {
    Copy-Item -Path "$resourcesPath\*" -Destination $OUT -Recurse -Force
}

# Update fabric.mod.json version
Write-Host ">> updating fabric.mod.json..."
$modJsonPath = "$OUT\fabric.mod.json"
if (Test-Path $modJsonPath) {
    try {
        $json = Get-Content $modJsonPath -Raw | ConvertFrom-Json
        $json.version = $MOD_VERSION
        $json.PSObject.Properties.Remove('icon')
        $json | ConvertTo-Json -Depth 10 | Set-Content $modJsonPath
    } catch {
        Write-Host "warning: failed to update fabric.mod.json version: $_" -ForegroundColor Yellow
    }
}

# Build jar
$JAR = "$DIST\$MOD_ID-$MOD_VERSION.jar"
Write-Host ">> building jar..."
$jarExe = "$JDK\bin\jar.exe"
Push-Location $OUT
& $jarExe --create --file $JAR -C . .
Pop-Location

if ($LASTEXITCODE -ne 0) {
    Write-Error "Jar creation failed with error code $LASTEXITCODE"
    exit $LASTEXITCODE
}

Write-Host ">> done: $JAR"

# Install to mods directory if specified
if ($env:INFINIZOOM_MODS_DIR) {
    Write-Host ">> installing to $env:INFINIZOOM_MODS_DIR"
    New-Item -ItemType Directory -Path $env:INFINIZOOM_MODS_DIR -Force | Out-Null
    Copy-Item -Path $JAR -Destination $env:INFINIZOOM_MODS_DIR -Force
}

Write-Host "Build completed successfully!"
