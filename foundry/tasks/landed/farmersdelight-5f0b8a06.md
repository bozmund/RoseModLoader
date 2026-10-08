---
id: farmersdelight-5f0b8a06
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/level/block/state/BlockState.m_60713_(Lnet/minecraft/world/level/block/Block;)Z
readable: net/minecraft/world/level/block/state/BlockState.is(Lnet/minecraft/world/level/block/Block;)Z
static: false
newOwner: net/minecraft/world/level/block/state/BlockState
uses: 40
usedIn: vectorwing/farmersdelight/common/block/AbstractStoveBlock,vectorwing/farmersdelight/common/block/BuddingBushBlock,vectorwing/farmersdelight/common/block/CabinetBlock,vectorwing/farmersdelight/common/block/CookingPotBlock,vectorwing/farmersdelight/common/block/CuttingBoardBlock,vectorwing/farmersdelight/common/block/HangingTomatoBlock,vectorwing/farmersdelight/common/block/RicePaniclesBlock,vectorwing/farmersdelight/common/block/RichSoilBlock,vectorwing/farmersdelight/common/block/RichSoilFarmlandBlock,vectorwing/farmersdelight/common/block/RopeBlock
shimClass: rose.era.v1_20_1.shim.BlockStateShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockStateShim.java
shimSignature: public static boolean is(net.minecraft.world.level.block.state.BlockState self, net.minecraft.world.level.block.Block block)
ruleLine: net/minecraft/world/level/block/state/BlockState.m_60713_(Lnet/minecraft/world/level/block/Block;)Z	rose/era/v1_20_1/shim/BlockStateShim.is	<evidence>
attempts: 2
created: 2026-10-09
lastFeedback: scope
landed: 2026-10-09 00:29
result: resolved; runtime findings 590 -> 589 (1 fixed)
---

# Redirect `net/minecraft/world/level/block/state/BlockState.is(Lnet/minecraft/world/level/block/Block;)Z`

The mod calls **net/minecraft/world/level/block/state/BlockState.is(Lnet/minecraft/world/level/block/Block;)Z** (40 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `BlockStateShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockStateShim.java`
- Signature: `public static boolean is(net.minecraft.world.level.block.state.BlockState self, net.minecraft.world.level.block.Block block)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/level/block/state/BlockState.m_60713_(Lnet/minecraft/world/level/block/Block;)Z<TAB>rose/era/v1_20_1/shim/BlockStateShim.is<TAB><evidence>`

## Landed 2026-10-09 00:29

Attempt 2 by script: C:/MyRepositories/Rose/foundry/test-agents/good-blockstate-is.sh. resolved; runtime findings 590 -> 589 (1 fixed)

Agent summary:

> script exited with 0: wrote eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockStateShim.java and the rule
