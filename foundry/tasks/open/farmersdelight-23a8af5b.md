---
id: farmersdelight-23a8af5b
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_MISSING
symbol: net/minecraft/client/model/geom/ModelLayers.m_247439_(Lnet/minecraft/world/level/block/state/properties/WoodType;)Lnet/minecraft/client/model/geom/ModelLayerLocation;
readable: net/minecraft/client/model/geom/ModelLayers.createHangingSignModelName(Lnet/minecraft/world/level/block/state/properties/WoodType;)Lnet/minecraft/client/model/geom/ModelLayerLocation;
static: true
newOwner: net/minecraft/client/model/geom/ModelLayers
uses: 1
usedIn: vectorwing/farmersdelight/client/renderer/HangingCanvasSignRenderer
shimClass: rose.era.v1_20_1.shim.ModelLayersShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ModelLayersShim.java
shimSignature: public static net.minecraft.client.model.geom.ModelLayerLocation createHangingSignModelName(net.minecraft.world.level.block.state.properties.WoodType woodType)
ruleLine: net/minecraft/client/model/geom/ModelLayers.m_247439_(Lnet/minecraft/world/level/block/state/properties/WoodType;)Lnet/minecraft/client/model/geom/ModelLayerLocation;	rose/era/v1_20_1/shim/ModelLayersShim.createHangingSignModelName	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/client/model/geom/ModelLayers.createHangingSignModelName(Lnet/minecraft/world/level/block/state/properties/WoodType;)Lnet/minecraft/client/model/geom/ModelLayerLocation;`

The mod calls **net/minecraft/client/model/geom/ModelLayers.createHangingSignModelName(Lnet/minecraft/world/level/block/state/properties/WoodType;)Lnet/minecraft/client/model/geom/ModelLayerLocation;** (1 uses), which can't be found in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `ModelLayersShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ModelLayersShim.java`
- Signature: `public static net.minecraft.client.model.geom.ModelLayerLocation createHangingSignModelName(net.minecraft.world.level.block.state.properties.WoodType woodType)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/client/model/geom/ModelLayers.m_247439_(Lnet/minecraft/world/level/block/state/properties/WoodType;)Lnet/minecraft/client/model/geom/ModelLayerLocation;<TAB>rose/era/v1_20_1/shim/ModelLayersShim.createHangingSignModelName<TAB><evidence>`
