---
id: farmersdelight-db7d0483
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/level/block/CrossCollisionBlock.m_7417_(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;
readable: net/minecraft/world/level/block/CrossCollisionBlock.updateShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;
static: false
newOwner: net/minecraft/world/level/block/CrossCollisionBlock
uses: 1
usedIn: vectorwing/farmersdelight/common/block/RopeFenceBlock
shimClass: rose.era.v1_20_1.shim.CrossCollisionBlockShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/CrossCollisionBlockShim.java
shimSignature: public static net.minecraft.world.level.block.state.BlockState updateShape(net.minecraft.world.level.block.CrossCollisionBlock self, net.minecraft.world.level.block.state.BlockState blockState, net.minecraft.core.Direction direction, net.minecraft.world.level.block.state.BlockState blockState2, net.minecraft.world.level.LevelAccessor levelAccessor, net.minecraft.core.BlockPos blockPos, net.minecraft.core.BlockPos blockPos2)
ruleLine: net/minecraft/world/level/block/CrossCollisionBlock.m_7417_(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;	rose/era/v1_20_1/shim/CrossCollisionBlockShim.updateShape	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/level/block/CrossCollisionBlock.updateShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;`

The mod calls **net/minecraft/world/level/block/CrossCollisionBlock.updateShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;** (1 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `CrossCollisionBlockShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/CrossCollisionBlockShim.java`
- Signature: `public static net.minecraft.world.level.block.state.BlockState updateShape(net.minecraft.world.level.block.CrossCollisionBlock self, net.minecraft.world.level.block.state.BlockState blockState, net.minecraft.core.Direction direction, net.minecraft.world.level.block.state.BlockState blockState2, net.minecraft.world.level.LevelAccessor levelAccessor, net.minecraft.core.BlockPos blockPos, net.minecraft.core.BlockPos blockPos2)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/level/block/CrossCollisionBlock.m_7417_(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;<TAB>rose/era/v1_20_1/shim/CrossCollisionBlockShim.updateShape<TAB><evidence>`
