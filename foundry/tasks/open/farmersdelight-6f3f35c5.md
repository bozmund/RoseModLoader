---
id: farmersdelight-6f3f35c5
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/level/block/HorizontalDirectionalBlock.m_5707_(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)V
readable: net/minecraft/world/level/block/HorizontalDirectionalBlock.playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)V
static: false
newOwner: net/minecraft/world/level/block/HorizontalDirectionalBlock
uses: 1
usedIn: vectorwing/farmersdelight/common/block/TatamiMatBlock
shimClass: rose.era.v1_20_1.shim.HorizontalDirectionalBlockShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/HorizontalDirectionalBlockShim.java
shimSignature: public static void playerWillDestroy(net.minecraft.world.level.block.HorizontalDirectionalBlock self, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos blockPos, net.minecraft.world.level.block.state.BlockState blockState, net.minecraft.world.entity.player.Player player)
ruleLine: net/minecraft/world/level/block/HorizontalDirectionalBlock.m_5707_(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)V	rose/era/v1_20_1/shim/HorizontalDirectionalBlockShim.playerWillDestroy	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/level/block/HorizontalDirectionalBlock.playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)V`

The mod calls **net/minecraft/world/level/block/HorizontalDirectionalBlock.playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)V** (1 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `HorizontalDirectionalBlockShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/HorizontalDirectionalBlockShim.java`
- Signature: `public static void playerWillDestroy(net.minecraft.world.level.block.HorizontalDirectionalBlock self, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos blockPos, net.minecraft.world.level.block.state.BlockState blockState, net.minecraft.world.entity.player.Player player)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/level/block/HorizontalDirectionalBlock.m_5707_(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)V<TAB>rose/era/v1_20_1/shim/HorizontalDirectionalBlockShim.playerWillDestroy<TAB><evidence>`
