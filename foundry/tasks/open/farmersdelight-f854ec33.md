---
id: farmersdelight-f854ec33
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/item/Item.m_41473_()Lnet/minecraft/world/food/FoodProperties;
readable: net/minecraft/world/item/Item.getFoodProperties()Lnet/minecraft/world/food/FoodProperties;
static: false
newOwner: net/minecraft/world/item/Item
uses: 1
usedIn: vectorwing/farmersdelight/common/block/PieBlock
shimClass: rose.era.v1_20_1.shim.ItemShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemShim.java
shimSignature: public static net.minecraft.world.food.FoodProperties getFoodProperties(net.minecraft.world.item.Item self)
ruleLine: net/minecraft/world/item/Item.m_41473_()Lnet/minecraft/world/food/FoodProperties;	rose/era/v1_20_1/shim/ItemShim.getFoodProperties	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/Item.getFoodProperties()Lnet/minecraft/world/food/FoodProperties;`

The mod calls **net/minecraft/world/item/Item.getFoodProperties()Lnet/minecraft/world/food/FoodProperties;** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `ItemShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemShim.java`
- Signature: `public static net.minecraft.world.food.FoodProperties getFoodProperties(net.minecraft.world.item.Item self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/Item.m_41473_()Lnet/minecraft/world/food/FoodProperties;<TAB>rose/era/v1_20_1/shim/ItemShim.getFoodProperties<TAB><evidence>`
