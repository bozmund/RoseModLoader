---
id: farmersdelight-f1cb0a98
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/level/block/BonemealableBlock.m_7370_(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Z
readable: net/minecraft/world/level/block/BonemealableBlock.isValidBonemealTarget(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Z
static: false
newOwner: net/minecraft/world/level/block/BonemealableBlock
uses: 2
usedIn: vectorwing/farmersdelight/common/block/RiceBlock,vectorwing/farmersdelight/common/block/RichSoilBlock
shimClass: rose.era.v1_20_1.shim.BonemealableBlockShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BonemealableBlockShim.java
shimSignature: public static boolean isValidBonemealTarget(net.minecraft.world.level.block.BonemealableBlock self, net.minecraft.world.level.LevelReader levelReader, net.minecraft.core.BlockPos blockPos, net.minecraft.world.level.block.state.BlockState blockState, boolean bValue)
ruleLine: net/minecraft/world/level/block/BonemealableBlock.m_7370_(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Z	rose/era/v1_20_1/shim/BonemealableBlockShim.isValidBonemealTarget	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/level/block/BonemealableBlock.isValidBonemealTarget(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Z`

The mod calls **net/minecraft/world/level/block/BonemealableBlock.isValidBonemealTarget(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Z** (2 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `BonemealableBlockShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BonemealableBlockShim.java`
- Signature: `public static boolean isValidBonemealTarget(net.minecraft.world.level.block.BonemealableBlock self, net.minecraft.world.level.LevelReader levelReader, net.minecraft.core.BlockPos blockPos, net.minecraft.world.level.block.state.BlockState blockState, boolean bValue)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/level/block/BonemealableBlock.m_7370_(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Z<TAB>rose/era/v1_20_1/shim/BonemealableBlockShim.isValidBonemealTarget<TAB><evidence>`
