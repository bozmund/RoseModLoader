---
id: farmersdelight-84b61c39
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/entity/player/Player.m_20193_()Lnet/minecraft/world/level/Level;
readable: net/minecraft/world/entity/player/Player.getCommandSenderWorld()Lnet/minecraft/world/level/Level;
static: false
newOwner: net/minecraft/world/entity/player/Player
uses: 2
usedIn: vectorwing/farmersdelight/common/item/SkilletItem$SkilletEvents
shimClass: rose.era.v1_20_1.shim.PlayerShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/PlayerShim.java
shimSignature: public static net.minecraft.world.level.Level getCommandSenderWorld(net.minecraft.world.entity.player.Player self)
ruleLine: net/minecraft/world/entity/player/Player.m_20193_()Lnet/minecraft/world/level/Level;	rose/era/v1_20_1/shim/PlayerShim.getCommandSenderWorld	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/entity/player/Player.getCommandSenderWorld()Lnet/minecraft/world/level/Level;`

The mod calls **net/minecraft/world/entity/player/Player.getCommandSenderWorld()Lnet/minecraft/world/level/Level;** (2 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `PlayerShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/PlayerShim.java`
- Signature: `public static net.minecraft.world.level.Level getCommandSenderWorld(net.minecraft.world.entity.player.Player self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/entity/player/Player.m_20193_()Lnet/minecraft/world/level/Level;<TAB>rose/era/v1_20_1/shim/PlayerShim.getCommandSenderWorld<TAB><evidence>`
