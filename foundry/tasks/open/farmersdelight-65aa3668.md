---
id: farmersdelight-65aa3668
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/food/FoodData.m_150378_(F)V
readable: net/minecraft/world/food/FoodData.setExhaustion(F)V
static: false
newOwner: net/minecraft/world/food/FoodData
uses: 1
usedIn: vectorwing/farmersdelight/common/effect/NourishmentEffect
shimClass: rose.era.v1_20_1.shim.FoodDataShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/FoodDataShim.java
shimSignature: public static void setExhaustion(net.minecraft.world.food.FoodData self, float fValue)
ruleLine: net/minecraft/world/food/FoodData.m_150378_(F)V	rose/era/v1_20_1/shim/FoodDataShim.setExhaustion	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/food/FoodData.setExhaustion(F)V`

The mod calls **net/minecraft/world/food/FoodData.setExhaustion(F)V** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `FoodDataShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/FoodDataShim.java`
- Signature: `public static void setExhaustion(net.minecraft.world.food.FoodData self, float fValue)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/food/FoodData.m_150378_(F)V<TAB>rose/era/v1_20_1/shim/FoodDataShim.setExhaustion<TAB><evidence>`
