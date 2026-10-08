---
id: farmersdelight-3cdb36f4
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/client/resources/model/BakedModel.m_7539_()Z
readable: net/minecraft/client/resources/model/BakedModel.isGui3d()Z
static: false
newOwner: net/minecraft/client/renderer/block/dispatch/BlockStateModel
uses: 1
usedIn: vectorwing/farmersdelight/client/renderer/CuttingBoardRenderer
shimClass: rose.era.v1_20_1.shim.BlockStateModelShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockStateModelShim.java
shimSignature: public static boolean isGui3d(net.minecraft.client.renderer.block.dispatch.BlockStateModel self)
ruleLine: net/minecraft/client/resources/model/BakedModel.m_7539_()Z	rose/era/v1_20_1/shim/BlockStateModelShim.isGui3d	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/client/resources/model/BakedModel.isGui3d()Z`

The mod calls **net/minecraft/client/resources/model/BakedModel.isGui3d()Z** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `BlockStateModelShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockStateModelShim.java`
- Signature: `public static boolean isGui3d(net.minecraft.client.renderer.block.dispatch.BlockStateModel self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/client/resources/model/BakedModel.m_7539_()Z<TAB>rose/era/v1_20_1/shim/BlockStateModelShim.isGui3d<TAB><evidence>`
