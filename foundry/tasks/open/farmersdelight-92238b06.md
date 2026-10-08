---
id: farmersdelight-92238b06
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_MISSING
symbol: net/minecraft/world/level/block/Block.m_5548_(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;)V
readable: net/minecraft/world/level/block/Block.updateEntityAfterFallOn(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;)V
static: false
newOwner: net/minecraft/world/level/block/Block
uses: 1
usedIn: vectorwing/farmersdelight/common/block/SafetyNetBlock
shimClass: rose.era.v1_20_1.shim.BlockShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockShim.java
shimSignature: public static void updateEntityAfterFallOn(net.minecraft.world.level.block.Block self, net.minecraft.world.level.BlockGetter blockGetter, net.minecraft.world.entity.Entity entity)
ruleLine: net/minecraft/world/level/block/Block.m_5548_(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;)V	rose/era/v1_20_1/shim/BlockShim.updateEntityAfterFallOn	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/level/block/Block.updateEntityAfterFallOn(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;)V`

The mod calls **net/minecraft/world/level/block/Block.updateEntityAfterFallOn(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;)V** (1 uses), which can't be found in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `BlockShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockShim.java`
- Signature: `public static void updateEntityAfterFallOn(net.minecraft.world.level.block.Block self, net.minecraft.world.level.BlockGetter blockGetter, net.minecraft.world.entity.Entity entity)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/level/block/Block.m_5548_(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;)V<TAB>rose/era/v1_20_1/shim/BlockShim.updateEntityAfterFallOn<TAB><evidence>`
