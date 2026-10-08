---
id: farmersdelight-bc8a0325
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/level/block/CropBlock.m_6227_(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;
readable: net/minecraft/world/level/block/CropBlock.use(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;
static: false
newOwner: net/minecraft/world/level/block/CropBlock
uses: 1
usedIn: vectorwing/farmersdelight/common/block/TomatoBlock
shimClass: rose.era.v1_20_1.shim.CropBlockShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/CropBlockShim.java
shimSignature: public static net.minecraft.world.InteractionResult use(net.minecraft.world.level.block.CropBlock self, net.minecraft.world.level.block.state.BlockState blockState, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos blockPos, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand interactionHand, net.minecraft.world.phys.BlockHitResult blockHitResult)
ruleLine: net/minecraft/world/level/block/CropBlock.m_6227_(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;	rose/era/v1_20_1/shim/CropBlockShim.use	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/level/block/CropBlock.use(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;`

The mod calls **net/minecraft/world/level/block/CropBlock.use(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `CropBlockShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/CropBlockShim.java`
- Signature: `public static net.minecraft.world.InteractionResult use(net.minecraft.world.level.block.CropBlock self, net.minecraft.world.level.block.state.BlockState blockState, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos blockPos, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand interactionHand, net.minecraft.world.phys.BlockHitResult blockHitResult)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/level/block/CropBlock.m_6227_(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;<TAB>rose/era/v1_20_1/shim/CropBlockShim.use<TAB><evidence>`
