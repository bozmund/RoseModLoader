---
id: farmersdelight-1fd7fddd
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/level/block/BushBlock.m_7892_(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)V
readable: net/minecraft/world/level/block/BushBlock.entityInside(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)V
static: false
newOwner: net/minecraft/world/level/block/VegetationBlock
uses: 1
usedIn: vectorwing/farmersdelight/common/block/BuddingBushBlock
shimClass: rose.era.v1_20_1.shim.VegetationBlockShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/VegetationBlockShim.java
shimSignature: public static void entityInside(net.minecraft.world.level.block.VegetationBlock self, net.minecraft.world.level.block.state.BlockState blockState, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos blockPos, net.minecraft.world.entity.Entity entity)
ruleLine: net/minecraft/world/level/block/BushBlock.m_7892_(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)V	rose/era/v1_20_1/shim/VegetationBlockShim.entityInside	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/level/block/BushBlock.entityInside(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)V`

The mod calls **net/minecraft/world/level/block/BushBlock.entityInside(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)V** (1 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `VegetationBlockShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/VegetationBlockShim.java`
- Signature: `public static void entityInside(net.minecraft.world.level.block.VegetationBlock self, net.minecraft.world.level.block.state.BlockState blockState, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos blockPos, net.minecraft.world.entity.Entity entity)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/level/block/BushBlock.m_7892_(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)V<TAB>rose/era/v1_20_1/shim/VegetationBlockShim.entityInside<TAB><evidence>`
