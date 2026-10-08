---
id: farmersdelight-ce76ecc2
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/level/LevelAccessor.m_142346_(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/gameevent/GameEvent;Lnet/minecraft/core/BlockPos;)V
readable: net/minecraft/world/level/LevelAccessor.gameEvent(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/gameevent/GameEvent;Lnet/minecraft/core/BlockPos;)V
static: false
newOwner: net/minecraft/world/level/LevelAccessor
uses: 2
usedIn: vectorwing/farmersdelight/common/block/AbstractStoveBlock
shimClass: rose.era.v1_20_1.shim.LevelAccessorShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/LevelAccessorShim.java
shimSignature: public static void gameEvent(net.minecraft.world.level.LevelAccessor self, net.minecraft.world.entity.Entity entity, net.minecraft.world.level.gameevent.GameEvent gameEvent, net.minecraft.core.BlockPos blockPos)
ruleLine: net/minecraft/world/level/LevelAccessor.m_142346_(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/gameevent/GameEvent;Lnet/minecraft/core/BlockPos;)V	rose/era/v1_20_1/shim/LevelAccessorShim.gameEvent	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/level/LevelAccessor.gameEvent(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/gameevent/GameEvent;Lnet/minecraft/core/BlockPos;)V`

The mod calls **net/minecraft/world/level/LevelAccessor.gameEvent(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/gameevent/GameEvent;Lnet/minecraft/core/BlockPos;)V** (2 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `LevelAccessorShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/LevelAccessorShim.java`
- Signature: `public static void gameEvent(net.minecraft.world.level.LevelAccessor self, net.minecraft.world.entity.Entity entity, net.minecraft.world.level.gameevent.GameEvent gameEvent, net.minecraft.core.BlockPos blockPos)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/level/LevelAccessor.m_142346_(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/gameevent/GameEvent;Lnet/minecraft/core/BlockPos;)V<TAB>rose/era/v1_20_1/shim/LevelAccessorShim.gameEvent<TAB><evidence>`
