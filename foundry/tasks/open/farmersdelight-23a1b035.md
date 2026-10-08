---
id: farmersdelight-23a1b035
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_MISSING
symbol: net/minecraft/core/BlockPos.m_252807_()Lnet/minecraft/world/phys/Vec3;
readable: net/minecraft/core/BlockPos.getCenter()Lnet/minecraft/world/phys/Vec3;
static: false
newOwner: net/minecraft/core/BlockPos
uses: 3
usedIn: vectorwing/farmersdelight/common/block/CuttingBoardBlock,vectorwing/farmersdelight/common/block/CuttingBoardBlock$ToolCarvingEvent
shimClass: rose.era.v1_20_1.shim.BlockPosShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockPosShim.java
shimSignature: public static net.minecraft.world.phys.Vec3 getCenter(net.minecraft.core.BlockPos self)
ruleLine: net/minecraft/core/BlockPos.m_252807_()Lnet/minecraft/world/phys/Vec3;	rose/era/v1_20_1/shim/BlockPosShim.getCenter	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/core/BlockPos.getCenter()Lnet/minecraft/world/phys/Vec3;`

The mod calls **net/minecraft/core/BlockPos.getCenter()Lnet/minecraft/world/phys/Vec3;** (3 uses), which can't be found in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `BlockPosShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockPosShim.java`
- Signature: `public static net.minecraft.world.phys.Vec3 getCenter(net.minecraft.core.BlockPos self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/core/BlockPos.m_252807_()Lnet/minecraft/world/phys/Vec3;<TAB>rose/era/v1_20_1/shim/BlockPosShim.getCenter<TAB><evidence>`
