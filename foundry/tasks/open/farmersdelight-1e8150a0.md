---
id: farmersdelight-1e8150a0
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/level/block/state/BlockState.m_60804_(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z
readable: net/minecraft/world/level/block/state/BlockState.isSolidRender(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z
static: false
newOwner: net/minecraft/world/level/block/state/BlockState
uses: 1
usedIn: vectorwing/farmersdelight/common/block/MushroomColonyBlock
shimClass: rose.era.v1_20_1.shim.BlockStateShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockStateShim.java
shimSignature: public static boolean isSolidRender(net.minecraft.world.level.block.state.BlockState self, net.minecraft.world.level.BlockGetter blockGetter, net.minecraft.core.BlockPos blockPos)
ruleLine: net/minecraft/world/level/block/state/BlockState.m_60804_(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z	rose/era/v1_20_1/shim/BlockStateShim.isSolidRender	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/level/block/state/BlockState.isSolidRender(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z`

The mod calls **net/minecraft/world/level/block/state/BlockState.isSolidRender(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z** (1 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `BlockStateShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockStateShim.java`
- Signature: `public static boolean isSolidRender(net.minecraft.world.level.block.state.BlockState self, net.minecraft.world.level.BlockGetter blockGetter, net.minecraft.core.BlockPos blockPos)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/level/block/state/BlockState.m_60804_(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z<TAB>rose/era/v1_20_1/shim/BlockStateShim.isSolidRender<TAB><evidence>`
