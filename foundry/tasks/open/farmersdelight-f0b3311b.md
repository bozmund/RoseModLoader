---
id: farmersdelight-f0b3311b
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/food/FoodProperties.m_38749_()Ljava/util/List;
readable: net/minecraft/world/food/FoodProperties.getEffects()Ljava/util/List;
static: false
newOwner: net/minecraft/world/food/FoodProperties
uses: 4
usedIn: vectorwing/farmersdelight/client/event/TooltipEvents,vectorwing/farmersdelight/common/block/PieBlock,vectorwing/farmersdelight/common/event/CommonEvents,vectorwing/farmersdelight/common/utility/TextUtils
shimClass: rose.era.v1_20_1.shim.FoodPropertiesShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/FoodPropertiesShim.java
shimSignature: public static java.util.List getEffects(net.minecraft.world.food.FoodProperties self)
ruleLine: net/minecraft/world/food/FoodProperties.m_38749_()Ljava/util/List;	rose/era/v1_20_1/shim/FoodPropertiesShim.getEffects	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/food/FoodProperties.getEffects()Ljava/util/List;`

The mod calls **net/minecraft/world/food/FoodProperties.getEffects()Ljava/util/List;** (4 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `FoodPropertiesShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/FoodPropertiesShim.java`
- Signature: `public static java.util.List getEffects(net.minecraft.world.food.FoodProperties self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/food/FoodProperties.m_38749_()Ljava/util/List;<TAB>rose/era/v1_20_1/shim/FoodPropertiesShim.getEffects<TAB><evidence>`
