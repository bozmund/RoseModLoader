---
id: farmersdelight-7e993431
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/client/model/geom/ModelLayers.m_171291_(Lnet/minecraft/world/level/block/state/properties/WoodType;)Lnet/minecraft/client/model/geom/ModelLayerLocation;
readable: net/minecraft/client/model/geom/ModelLayers.createSignModelName(Lnet/minecraft/world/level/block/state/properties/WoodType;)Lnet/minecraft/client/model/geom/ModelLayerLocation;
static: true
newOwner: net/minecraft/client/model/geom/ModelLayers
uses: 1
usedIn: vectorwing/farmersdelight/client/renderer/CanvasSignRenderer
shimClass: rose.era.v1_20_1.shim.ModelLayersShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ModelLayersShim.java
shimSignature: public static net.minecraft.client.model.geom.ModelLayerLocation createSignModelName(net.minecraft.world.level.block.state.properties.WoodType woodType)
ruleLine: net/minecraft/client/model/geom/ModelLayers.m_171291_(Lnet/minecraft/world/level/block/state/properties/WoodType;)Lnet/minecraft/client/model/geom/ModelLayerLocation;	rose/era/v1_20_1/shim/ModelLayersShim.createSignModelName	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/client/model/geom/ModelLayers.createSignModelName(Lnet/minecraft/world/level/block/state/properties/WoodType;)Lnet/minecraft/client/model/geom/ModelLayerLocation;`

The mod calls **net/minecraft/client/model/geom/ModelLayers.createSignModelName(Lnet/minecraft/world/level/block/state/properties/WoodType;)Lnet/minecraft/client/model/geom/ModelLayerLocation;** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `ModelLayersShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ModelLayersShim.java`
- Signature: `public static net.minecraft.client.model.geom.ModelLayerLocation createSignModelName(net.minecraft.world.level.block.state.properties.WoodType woodType)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/client/model/geom/ModelLayers.m_171291_(Lnet/minecraft/world/level/block/state/properties/WoodType;)Lnet/minecraft/client/model/geom/ModelLayerLocation;<TAB>rose/era/v1_20_1/shim/ModelLayersShim.createSignModelName<TAB><evidence>`
