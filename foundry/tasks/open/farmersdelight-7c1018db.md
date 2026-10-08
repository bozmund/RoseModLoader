---
id: farmersdelight-7c1018db
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/level/block/Block.m_142072_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;F)V
readable: net/minecraft/world/level/block/Block.fallOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;F)V
static: false
newOwner: net/minecraft/world/level/block/Block
uses: 1
usedIn: vectorwing/farmersdelight/common/block/SafetyNetBlock
shimClass: rose.era.v1_20_1.shim.BlockShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockShim.java
shimSignature: public static void fallOn(net.minecraft.world.level.block.Block self, net.minecraft.world.level.Level level, net.minecraft.world.level.block.state.BlockState blockState, net.minecraft.core.BlockPos blockPos, net.minecraft.world.entity.Entity entity, float fValue)
ruleLine: net/minecraft/world/level/block/Block.m_142072_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;F)V	rose/era/v1_20_1/shim/BlockShim.fallOn	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/level/block/Block.fallOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;F)V`

The mod calls **net/minecraft/world/level/block/Block.fallOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;F)V** (1 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `BlockShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockShim.java`
- Signature: `public static void fallOn(net.minecraft.world.level.block.Block self, net.minecraft.world.level.Level level, net.minecraft.world.level.block.state.BlockState blockState, net.minecraft.core.BlockPos blockPos, net.minecraft.world.entity.Entity entity, float fValue)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/level/block/Block.m_142072_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;F)V<TAB>rose/era/v1_20_1/shim/BlockShim.fallOn<TAB><evidence>`
