---
id: farmersdelight-bac96bd1
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/core/BlockSource.m_7727_()Lnet/minecraft/server/level/ServerLevel;
readable: net/minecraft/core/BlockSource.getLevel()Lnet/minecraft/server/level/ServerLevel;
static: false
newOwner: net/minecraft/core/dispenser/BlockSource
uses: 1
usedIn: vectorwing/farmersdelight/common/block/entity/dispenser/CuttingBoardDispenseBehavior
shimClass: rose.era.v1_20_1.shim.BlockSourceShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockSourceShim.java
shimSignature: public static net.minecraft.server.level.ServerLevel getLevel(net.minecraft.core.dispenser.BlockSource self)
ruleLine: net/minecraft/core/BlockSource.m_7727_()Lnet/minecraft/server/level/ServerLevel;	rose/era/v1_20_1/shim/BlockSourceShim.getLevel	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/core/BlockSource.getLevel()Lnet/minecraft/server/level/ServerLevel;`

The mod calls **net/minecraft/core/BlockSource.getLevel()Lnet/minecraft/server/level/ServerLevel;** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `BlockSourceShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockSourceShim.java`
- Signature: `public static net.minecraft.server.level.ServerLevel getLevel(net.minecraft.core.dispenser.BlockSource self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/core/BlockSource.m_7727_()Lnet/minecraft/server/level/ServerLevel;<TAB>rose/era/v1_20_1/shim/BlockSourceShim.getLevel<TAB><evidence>`
