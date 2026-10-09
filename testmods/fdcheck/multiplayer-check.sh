#!/bin/bash
# Farmer's Delight multiplayer check, driven through the Agent Bridge: a dedicated server and a real client.
#
# Needs: the FD 1.20.1 jar in run/server/mods and run/client/mods, eula=true in run/server/eula.txt (accept it
# yourself), and for this local test server online-mode=false and white-list=false (the dev client is offline).
# Run from the repo root: bash testmods/fdcheck/multiplayer-check.sh
set -u
cd "$(dirname "$0")/../.."
SERVER="--target server"
CLIENT="--target client"
FAILED=0

check() { # name, condition result ("ok" or anything else)
    if [ "$2" = "ok" ]; then echo "PASS $1"; else echo "FAIL $1 ($2)"; FAILED=1; fi
}
has() { grep -q "$1" <<<"$2" && echo ok || echo "missing $1"; }

./rose stop client >/dev/null 2>&1; ./rose stop server >/dev/null 2>&1
timeout 900 ./rose launch server >/dev/null 2>&1
for _ in $(seq 1 120); do ./rose ctl server.status $SERVER 2>/dev/null | grep -q '"running": true' && break; sleep 3; done
check "dedicated server starts with FD" "$(has '"running": true' "$(./rose ctl server.status $SERVER 2>&1)")"

timeout 600 ./rose launch client >/dev/null 2>&1
check "client joins" "$(has '"inWorld": true' "$(timeout 300 ./rose ctl client.connect $CLIENT address=localhost 2>&1)")"

read -r X Y Z < <(./rose ctl server.players $SERVER 2>&1 | python -c \
    "import json,sys;p=[x for x in json.load(sys.stdin) if not x['bot']][0];print(int(p['x']),int(p['y']),int(p['z']),end='')")
NAME=$(./rose ctl server.players $SERVER 2>&1 | python -c "import json,sys;print([x for x in json.load(sys.stdin) if not x['bot']][0]['name'],end='')")
cmd() { ./rose ctl server.command $SERVER command="$1" >/dev/null; }
BX=$((X + 1)); BZ=$((Z - 2)); PX=$((X - 1))
cmd "gamemode survival $NAME"
cmd "fill $((X - 3)) $Y $((Z - 4)) $((X + 3)) $((Y + 3)) $((Z + 1)) air"
cmd "fill $((X - 3)) $((Y - 1)) $((Z - 4)) $((X + 3)) $((Y - 1)) $((Z + 1)) stone"
cmd "setblock $BX $Y $BZ farmersdelight:cutting_board"
cmd "setblock $PX $((Y - 1)) $BZ farmersdelight:stove[lit=true]"
cmd "setblock $PX $Y $BZ farmersdelight:cooking_pot"
cmd "tp $NAME $X $Y $Z 180 30"

cmd "item replace entity $NAME weapon.mainhand with farmersdelight:cabbage"; sleep 1
./rose ctl client.useBlock $CLIENT x=$BX y=$Y z=$BZ face=up >/dev/null; sleep 1
check "cabbage on the cutting board reaches the client" \
    "$(has 'farmersdelight:cabbage' "$(./rose ctl client.getBlock $CLIENT x=$BX y=$Y z=$BZ 2>&1)")"

cmd "item replace entity $NAME weapon.mainhand with farmersdelight:iron_knife"; sleep 1
./rose ctl client.useBlock $CLIENT x=$BX y=$Y z=$BZ face=up >/dev/null; sleep 2
check "knife cuts it into cabbage leaves (server)" "$(has 'passed' "$(./rose ctl server.command $SERVER \
    command='execute if entity @e[type=item,nbt={Item:{id:"farmersdelight:cabbage_leaf"}}]' 2>&1)")"

cmd "item replace entity $NAME weapon.mainhand with minecraft:air"; sleep 1
./rose ctl client.useBlock $CLIENT x=$PX y=$Y z=$BZ face=south >/dev/null; sleep 2
check "cooking pot opens FD's screen on the client" \
    "$(has 'CookingPotScreen' "$(./rose ctl client.screen $CLIENT 2>&1)")"
./rose ctl client.chat $CLIENT message="/say closing" >/dev/null 2>&1

./rose ctl bot.spawn $SERVER name=Chef x=$BX y=$Y z=$((Z - 1)) >/dev/null
cmd "item replace entity Chef weapon.mainhand with farmersdelight:tomato"
./rose ctl player.useBlock $SERVER player=Chef x=$BX y=$Y z=$BZ face=up >/dev/null; sleep 2
check "a second player's tomato on the board reaches the client" \
    "$(has 'farmersdelight:tomato' "$(./rose ctl client.getBlock $CLIENT x=$BX y=$Y z=$BZ 2>&1)")"

./rose stop client >/dev/null 2>&1; ./rose stop server >/dev/null 2>&1
[ $FAILED = 0 ] && echo "ALL PASSED" || echo "SOME FAILED"
exit $FAILED
