---
id: farmersdelight-707fa12d
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/food/FoodProperties$Builder.m_38766_()Lnet/minecraft/world/food/FoodProperties$Builder;
readable: net/minecraft/world/food/FoodProperties$Builder.fast()Lnet/minecraft/world/food/FoodProperties$Builder;
static: false
newOwner: net/minecraft/world/food/FoodProperties$Builder
uses: 18
usedIn: vectorwing/farmersdelight/common/FoodValues
shimClass: rose.era.v1_20_1.shim.FoodPropertiesBuilderShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/FoodPropertiesBuilderShim.java
shimSignature: public static net.minecraft.world.food.FoodProperties.Builder fast(net.minecraft.world.food.FoodProperties.Builder self)
ruleLine: net/minecraft/world/food/FoodProperties$Builder.m_38766_()Lnet/minecraft/world/food/FoodProperties$Builder;	rose/era/v1_20_1/shim/FoodPropertiesBuilderShim.fast	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/food/FoodProperties$Builder.fast()Lnet/minecraft/world/food/FoodProperties$Builder;`

The mod calls **net/minecraft/world/food/FoodProperties$Builder.fast()Lnet/minecraft/world/food/FoodProperties$Builder;** (18 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `FoodPropertiesBuilderShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/FoodPropertiesBuilderShim.java`
- Signature: `public static net.minecraft.world.food.FoodProperties.Builder fast(net.minecraft.world.food.FoodProperties.Builder self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/food/FoodProperties$Builder.m_38766_()Lnet/minecraft/world/food/FoodProperties$Builder;<TAB>rose/era/v1_20_1/shim/FoodPropertiesBuilderShim.fast<TAB><evidence>`
