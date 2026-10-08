---
id: farmersdelight-a720bf5c
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_MISSING
symbol: net/minecraft/world/level/levelgen/feature/stateproviders/BlockStateProvider.m_191382_(Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/level/levelgen/feature/stateproviders/SimpleStateProvider;
readable: net/minecraft/world/level/levelgen/feature/stateproviders/BlockStateProvider.simple(Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/level/levelgen/feature/stateproviders/SimpleStateProvider;
static: true
newOwner: net/minecraft/world/level/levelgen/feature/stateproviders/BlockStateProvider
uses: 1
usedIn: vectorwing/farmersdelight/common/world/WildCropGeneration
shimClass: rose.era.v1_20_1.shim.BlockStateProviderShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockStateProviderShim.java
shimSignature: public static net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider simple(net.minecraft.world.level.block.Block block)
ruleLine: net/minecraft/world/level/levelgen/feature/stateproviders/BlockStateProvider.m_191382_(Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/level/levelgen/feature/stateproviders/SimpleStateProvider;	rose/era/v1_20_1/shim/BlockStateProviderShim.simple	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/level/levelgen/feature/stateproviders/BlockStateProvider.simple(Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/level/levelgen/feature/stateproviders/SimpleStateProvider;`

The mod calls **net/minecraft/world/level/levelgen/feature/stateproviders/BlockStateProvider.simple(Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/level/levelgen/feature/stateproviders/SimpleStateProvider;** (1 uses), which can't be found in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `BlockStateProviderShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockStateProviderShim.java`
- Signature: `public static net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider simple(net.minecraft.world.level.block.Block block)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/level/levelgen/feature/stateproviders/BlockStateProvider.m_191382_(Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/level/levelgen/feature/stateproviders/SimpleStateProvider;<TAB>rose/era/v1_20_1/shim/BlockStateProviderShim.simple<TAB><evidence>`
