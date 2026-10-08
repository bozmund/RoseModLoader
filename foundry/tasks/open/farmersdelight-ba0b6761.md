---
id: farmersdelight-ba0b6761
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/level/Level.m_46469_()Lnet/minecraft/world/level/GameRules;
readable: net/minecraft/world/level/Level.getGameRules()Lnet/minecraft/world/level/GameRules;
static: false
newOwner: net/minecraft/world/level/Level
uses: 2
usedIn: vectorwing/farmersdelight/client/gui/NourishmentHungerOverlay,vectorwing/farmersdelight/common/effect/NourishmentEffect
shimClass: rose.era.v1_20_1.shim.LevelShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/LevelShim.java
shimSignature: public static net.minecraft.world.level.gamerules.GameRules getGameRules(net.minecraft.world.level.Level self)
ruleLine: net/minecraft/world/level/Level.m_46469_()Lnet/minecraft/world/level/GameRules;	rose/era/v1_20_1/shim/LevelShim.getGameRules	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/level/Level.getGameRules()Lnet/minecraft/world/level/GameRules;`

The mod calls **net/minecraft/world/level/Level.getGameRules()Lnet/minecraft/world/level/GameRules;** (2 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `LevelShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/LevelShim.java`
- Signature: `public static net.minecraft.world.level.gamerules.GameRules getGameRules(net.minecraft.world.level.Level self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/level/Level.m_46469_()Lnet/minecraft/world/level/GameRules;<TAB>rose/era/v1_20_1/shim/LevelShim.getGameRules<TAB><evidence>`
