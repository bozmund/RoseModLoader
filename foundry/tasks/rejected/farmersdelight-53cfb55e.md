---
id: farmersdelight-53cfb55e
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/level/block/entity/ContainerOpenersCounter.m_155468_(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V
readable: net/minecraft/world/level/block/entity/ContainerOpenersCounter.decrementOpeners(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V
static: false
newOwner: net/minecraft/world/level/block/entity/ContainerOpenersCounter
uses: 1
usedIn: vectorwing/farmersdelight/common/block/entity/CabinetBlockEntity
shimClass: rose.era.v1_20_1.shim.ContainerOpenersCounterShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ContainerOpenersCounterShim.java
shimSignature: public static void decrementOpeners(net.minecraft.world.level.block.entity.ContainerOpenersCounter self, net.minecraft.world.entity.player.Player player, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos blockPos, net.minecraft.world.level.block.state.BlockState blockState)
ruleLine: net/minecraft/world/level/block/entity/ContainerOpenersCounter.m_155468_(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V	rose/era/v1_20_1/shim/ContainerOpenersCounterShim.decrementOpeners	<evidence>
attempts: 0
created: 2026-10-09
closedReason: resolved-elsewhere
---

# Redirect `net/minecraft/world/level/block/entity/ContainerOpenersCounter.decrementOpeners(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V`

The mod calls **net/minecraft/world/level/block/entity/ContainerOpenersCounter.decrementOpeners(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V** (1 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `ContainerOpenersCounterShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ContainerOpenersCounterShim.java`
- Signature: `public static void decrementOpeners(net.minecraft.world.level.block.entity.ContainerOpenersCounter self, net.minecraft.world.entity.player.Player player, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos blockPos, net.minecraft.world.level.block.state.BlockState blockState)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/level/block/entity/ContainerOpenersCounter.m_155468_(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V<TAB>rose/era/v1_20_1/shim/ContainerOpenersCounterShim.decrementOpeners<TAB><evidence>`

## Closed 2026-10-09 00:38

No longer reported by `rose analyze` (resolved elsewhere).
