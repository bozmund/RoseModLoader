---
id: farmersdelight-aae06141
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/food/FoodData.m_38712_(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;)V
readable: net/minecraft/world/food/FoodData.eat(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;)V
static: false
newOwner: net/minecraft/world/food/FoodData
uses: 1
usedIn: vectorwing/farmersdelight/common/block/PieBlock
shimClass: rose.era.v1_20_1.shim.FoodDataShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/FoodDataShim.java
shimSignature: public static void eat(net.minecraft.world.food.FoodData self, net.minecraft.world.item.Item item, net.minecraft.world.item.ItemStack itemStack)
ruleLine: net/minecraft/world/food/FoodData.m_38712_(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;)V	rose/era/v1_20_1/shim/FoodDataShim.eat	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/food/FoodData.eat(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;)V`

The mod calls **net/minecraft/world/food/FoodData.eat(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;)V** (1 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `FoodDataShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/FoodDataShim.java`
- Signature: `public static void eat(net.minecraft.world.food.FoodData self, net.minecraft.world.item.Item item, net.minecraft.world.item.ItemStack itemStack)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/food/FoodData.m_38712_(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;)V<TAB>rose/era/v1_20_1/shim/FoodDataShim.eat<TAB><evidence>`
