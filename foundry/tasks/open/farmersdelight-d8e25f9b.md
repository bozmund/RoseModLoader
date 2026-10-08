---
id: farmersdelight-d8e25f9b
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/level/Level.m_46467_()J
readable: net/minecraft/world/level/Level.getGameTime()J
static: false
newOwner: net/minecraft/world/level/Level
uses: 3
usedIn: vectorwing/farmersdelight/client/renderer/SkilletItemRenderer$ArmPoseTransformer,vectorwing/farmersdelight/common/item/SkilletItem,vectorwing/farmersdelight/common/network/ModNetworking$FlipSkilletMessage
shimClass: rose.era.v1_20_1.shim.LevelShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/LevelShim.java
shimSignature: public static long getGameTime(net.minecraft.world.level.Level self)
ruleLine: net/minecraft/world/level/Level.m_46467_()J	rose/era/v1_20_1/shim/LevelShim.getGameTime	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/level/Level.getGameTime()J`

The mod calls **net/minecraft/world/level/Level.getGameTime()J** (3 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `LevelShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/LevelShim.java`
- Signature: `public static long getGameTime(net.minecraft.world.level.Level self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/level/Level.m_46467_()J<TAB>rose/era/v1_20_1/shim/LevelShim.getGameTime<TAB><evidence>`
