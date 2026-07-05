#!/usr/bin/env bash
set -euo pipefail

PROJ="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

MOD_ID="infinizoom"
MOD_VERSION="${MOD_VERSION:-1.0.2}"

JDK="${JAVA_HOME:-}"
if [ -z "$JDK" ]; then
    if command -v javac >/dev/null 2>&1; then
        JDK="$(dirname "$(dirname "$(readlink -f "$(command -v javac)")")")"
    else
        echo "error: no JDK found. Set JAVA_HOME (Java 25+) or put javac on PATH." >&2
        exit 1
    fi
fi

LIBS="${INFINIZOOM_LIBS:-$PROJ/libs}"
if [ ! -d "$LIBS" ]; then
    cat >&2 <<EOF
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

  INFINIZOOM_LIBS=/path/to/libs ./build-manual.sh

Or drop the jars in ./libs next to this script. For normal setups, prefer the
Gradle/Loom build (./gradlew build) once Loom supports your version.
EOF
    exit 1
fi

OUT="$PROJ/build/manual/classes"
DIST="$PROJ/build/manual/libs"
rm -rf "$OUT" "$DIST"; mkdir -p "$OUT" "$DIST"

CP="$(find "$LIBS" -name '*.jar' ! -name '*natives*' | paste -sd:)"
if [ -z "$CP" ]; then
    echo "error: no .jar files found in $LIBS" >&2
    exit 1
fi

echo ">> compiling..."
"$JDK/bin/javac" --release 25 -cp "$CP" -d "$OUT" $(find "$PROJ/src/main/java" -name '*.java')

echo ">> packaging resources..."
cp -r "$PROJ/src/main/resources/"* "$OUT/"
python3 - "$OUT/fabric.mod.json" "$MOD_VERSION" <<'PY'
import json,sys
p,ver=sys.argv[1],sys.argv[2]
d=json.load(open(p)); d["version"]=ver; d.pop("icon",None)
json.dump(d,open(p,"w"),indent=2)
PY

JAR="$DIST/$MOD_ID-$MOD_VERSION.jar"
echo ">> building jar..."
"$JDK/bin/jar" --create --file "$JAR" -C "$OUT" .

echo ">> done: $JAR"

if [ -n "${INFINIZOOM_MODS_DIR:-}" ]; then
    echo ">> installing to $INFINIZOOM_MODS_DIR"
    mkdir -p "$INFINIZOOM_MODS_DIR"
    cp "$JAR" "$INFINIZOOM_MODS_DIR/"
fi
