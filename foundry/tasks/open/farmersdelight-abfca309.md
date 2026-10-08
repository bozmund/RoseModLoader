---
id: farmersdelight-abfca309
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_MISSING
symbol: net/minecraft/client/renderer/LevelRenderer.m_109541_(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I
readable: net/minecraft/client/renderer/LevelRenderer.getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I
static: true
newOwner: net/minecraft/client/renderer/LevelRenderer
uses: 1
usedIn: vectorwing/farmersdelight/client/renderer/DefaultStoveRenderer
shimClass: rose.era.v1_20_1.shim.LevelRendererShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/LevelRendererShim.java
shimSignature: public static int getLightColor(net.minecraft.client.renderer.block.BlockAndTintGetter blockAndTintGetter, net.minecraft.core.BlockPos blockPos)
ruleLine: net/minecraft/client/renderer/LevelRenderer.m_109541_(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I	rose/era/v1_20_1/shim/LevelRendererShim.getLightColor	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/client/renderer/LevelRenderer.getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I`

The mod calls **net/minecraft/client/renderer/LevelRenderer.getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I** (1 uses), which can't be found in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `LevelRendererShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/LevelRendererShim.java`
- Signature: `public static int getLightColor(net.minecraft.client.renderer.block.BlockAndTintGetter blockAndTintGetter, net.minecraft.core.BlockPos blockPos)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/client/renderer/LevelRenderer.m_109541_(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I<TAB>rose/era/v1_20_1/shim/LevelRendererShim.getLightColor<TAB><evidence>`
