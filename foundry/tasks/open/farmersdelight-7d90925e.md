---
id: farmersdelight-7d90925e
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/InteractionResult.m_19078_(Z)Lnet/minecraft/world/InteractionResult;
readable: net/minecraft/world/InteractionResult.sidedSuccess(Z)Lnet/minecraft/world/InteractionResult;
static: true
newOwner: net/minecraft/world/InteractionResult
uses: 5
usedIn: vectorwing/farmersdelight/common/block/AbstractStoveBlock,vectorwing/farmersdelight/common/block/MushroomColonyBlock,vectorwing/farmersdelight/common/block/RopeBlock
shimClass: rose.era.v1_20_1.shim.InteractionResultShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/InteractionResultShim.java
shimSignature: public static net.minecraft.world.InteractionResult sidedSuccess(boolean bValue)
ruleLine: net/minecraft/world/InteractionResult.m_19078_(Z)Lnet/minecraft/world/InteractionResult;	rose/era/v1_20_1/shim/InteractionResultShim.sidedSuccess	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/InteractionResult.sidedSuccess(Z)Lnet/minecraft/world/InteractionResult;`

The mod calls **net/minecraft/world/InteractionResult.sidedSuccess(Z)Lnet/minecraft/world/InteractionResult;** (5 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `InteractionResultShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/InteractionResultShim.java`
- Signature: `public static net.minecraft.world.InteractionResult sidedSuccess(boolean bValue)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/InteractionResult.m_19078_(Z)Lnet/minecraft/world/InteractionResult;<TAB>rose/era/v1_20_1/shim/InteractionResultShim.sidedSuccess<TAB><evidence>`
