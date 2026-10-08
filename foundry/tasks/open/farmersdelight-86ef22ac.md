---
id: farmersdelight-86ef22ac
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/level/block/BaseEntityBlock.m_6810_(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V
readable: net/minecraft/world/level/block/BaseEntityBlock.onRemove(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V
static: false
newOwner: net/minecraft/world/level/block/BaseEntityBlock
uses: 5
usedIn: vectorwing/farmersdelight/common/block/AbstractStoveBlock,vectorwing/farmersdelight/common/block/BasketBlock,vectorwing/farmersdelight/common/block/CabinetBlock,vectorwing/farmersdelight/common/block/CuttingBoardBlock,vectorwing/farmersdelight/common/block/SkilletBlock
shimClass: rose.era.v1_20_1.shim.BaseEntityBlockShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BaseEntityBlockShim.java
shimSignature: public static void onRemove(net.minecraft.world.level.block.BaseEntityBlock self, net.minecraft.world.level.block.state.BlockState blockState, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos blockPos, net.minecraft.world.level.block.state.BlockState blockState2, boolean bValue)
ruleLine: net/minecraft/world/level/block/BaseEntityBlock.m_6810_(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V	rose/era/v1_20_1/shim/BaseEntityBlockShim.onRemove	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/level/block/BaseEntityBlock.onRemove(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V`

The mod calls **net/minecraft/world/level/block/BaseEntityBlock.onRemove(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V** (5 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `BaseEntityBlockShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BaseEntityBlockShim.java`
- Signature: `public static void onRemove(net.minecraft.world.level.block.BaseEntityBlock self, net.minecraft.world.level.block.state.BlockState blockState, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos blockPos, net.minecraft.world.level.block.state.BlockState blockState2, boolean bValue)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/level/block/BaseEntityBlock.m_6810_(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V<TAB>rose/era/v1_20_1/shim/BaseEntityBlockShim.onRemove<TAB><evidence>`
