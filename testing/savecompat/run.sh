#!/usr/bin/env bash
# Save compatibility check: a Farmer's Delight 1.20.1 world opens on Rose with its FD blocks and block entity contents.
#
# 1. A vanilla 1.20.1 server (corpus/minecraft/1.20.1/server-bundler.jar) makes a flat world.
# 2. make_fd_world.py writes FD 1.20.1 blocks and block entities into it, in Forge 1.20.1's save format.
# 3. Rose's dedicated server (run/server, with FD in run/server/mods) opens it; the Agent Bridge reads the blocks back.
#
# Both servers need Mojang's EULA accepted: set ROSE_ACCEPT_EULA=1 to accept it for the two server directories.
# Needs python3 with nbtlib. Exit status 0 when every check passes.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"
JAVA="${JAVA_HOME:+$JAVA_HOME/bin/}java"
WORK="$ROOT/build/savecompat"
WORLD=savecompat_fd120

if [ "${ROSE_ACCEPT_EULA:-}" != "1" ]; then
  echo "Set ROSE_ACCEPT_EULA=1 to accept Mojang's EULA (https://aka.ms/MinecraftEULA) for the test servers." >&2
  exit 2
fi
ls run/server/mods/FarmersDelight-1.20.1-*.jar >/dev/null 2>&1 || { echo "put the FD 1.20.1 jar in run/server/mods" >&2; exit 2; }

rm -rf "$WORK" && mkdir -p "$WORK/vanilla"
( cd "$WORK/vanilla"
  echo "eula=true" > eula.txt
  printf 'level-type=minecraft\\:flat\nonline-mode=false\nlevel-name=%s\n' "$WORLD" > server.properties
  echo stop | "$JAVA" -Xmx2G -jar "$ROOT/corpus/minecraft/1.20.1/server-bundler.jar" nogui > vanilla.log 2>&1 )
python3 testing/savecompat/make_fd_world.py "$WORK/vanilla/$WORLD"

mkdir -p run/server && echo "eula=true" > run/server/eula.txt
rm -rf "run/server/$WORLD" && cp -r "$WORK/vanilla/$WORLD" "run/server/$WORLD"
touch run/server/server.properties && cp run/server/server.properties "$WORK/server.properties"
trap 'cp "$WORK/server.properties" run/server/server.properties; ./rose stop server >/dev/null 2>&1 || true' EXIT
grep -v '^level-name=\|^white-list=' "$WORK/server.properties" > run/server/server.properties || true
printf 'level-name=%s\nwhite-list=false\n' "$WORLD" >> run/server/server.properties

./rose launch server > "$WORK/launch.log" 2>&1
for _ in $(seq 1 100); do ./rose ctl --target server server.status 2>/dev/null | grep -q '"running": *true' && break; sleep 3; done

block() { ./rose ctl --target server world.getBlock x="$1" y=-60 z=2 2>/dev/null | tr -d '\n'; }
failures=0
check() { # name, x, expected substrings...
  local name="$1" x="$2"; shift 2
  local got; got="$(block "$x")"
  for want in "$@"; do
    if [[ "$got" != *"$want"* ]]; then echo "FAIL $name: no $want in $got"; failures=$((failures + 1)); return; fi
  done
  echo "ok   $name"
}
check "cooking pot keeps its stacks"  2 'farmersdelight:cooking_pot' 'count:3,id:\"minecraft:carrot\"' 'count:2,id:\"farmersdelight:beef_stew\"' 'Container:{count:1,id:\"minecraft:bowl\"}'
check "cutting board keeps its item"   4 'farmersdelight:cutting_board' 'id:\"farmersdelight:cabbage\"'
check "old basket id loads (alias)"    6 'farmersdelight:bamboo_basket' 'count:5,id:\"farmersdelight:tomato\"'
check "canvas sign text is upgraded"   8 'farmersdelight:canvas_sign' 'messages:[\"Rose\",\"loads\",\"FD\",\"\"]'
check "chest keeps FD items' data"    10 '\"minecraft:damage\":7' 'id:\"farmersdelight:skillet\"' '\"minecraft:custom_name\":\"Old knife\"'
check "rich soil"                     12 'farmersdelight:rich_soil'
echo "$failures check(s) failed"
exit $((failures > 0))
