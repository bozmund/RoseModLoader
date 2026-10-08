---
id: farmersdelight-d35ca089
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/level/levelgen/blockpredicates/BlockPredicate.m_224774_(Lnet/minecraft/core/Vec3i;[Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/level/levelgen/blockpredicates/BlockPredicate;
readable: net/minecraft/world/level/levelgen/blockpredicates/BlockPredicate.matchesBlocks(Lnet/minecraft/core/Vec3i;[Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/level/levelgen/blockpredicates/BlockPredicate;
static: true
newOwner: net/minecraft/world/level/levelgen/blockpredicates/BlockPredicate
uses: 1
usedIn: vectorwing/farmersdelight/common/world/WildCropGeneration
shimClass: rose.era.v1_20_1.shim.BlockPredicateShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockPredicateShim.java
shimSignature: public static net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate matchesBlocks(net.minecraft.core.Vec3i vec3i, net.minecraft.world.level.block.Block[] blocks)
ruleLine: net/minecraft/world/level/levelgen/blockpredicates/BlockPredicate.m_224774_(Lnet/minecraft/core/Vec3i;[Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/level/levelgen/blockpredicates/BlockPredicate;	rose/era/v1_20_1/shim/BlockPredicateShim.matchesBlocks	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/level/levelgen/blockpredicates/BlockPredicate.matchesBlocks(Lnet/minecraft/core/Vec3i;[Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/level/levelgen/blockpredicates/BlockPredicate;`

The mod calls **net/minecraft/world/level/levelgen/blockpredicates/BlockPredicate.matchesBlocks(Lnet/minecraft/core/Vec3i;[Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/level/levelgen/blockpredicates/BlockPredicate;** (1 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `BlockPredicateShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockPredicateShim.java`
- Signature: `public static net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate matchesBlocks(net.minecraft.core.Vec3i vec3i, net.minecraft.world.level.block.Block[] blocks)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/level/levelgen/blockpredicates/BlockPredicate.m_224774_(Lnet/minecraft/core/Vec3i;[Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/level/levelgen/blockpredicates/BlockPredicate;<TAB>rose/era/v1_20_1/shim/BlockPredicateShim.matchesBlocks<TAB><evidence>`
