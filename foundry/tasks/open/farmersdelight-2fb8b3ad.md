---
id: farmersdelight-2fb8b3ad
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/item/crafting/Ingredient.m_43927_([Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/crafting/Ingredient;
readable: net/minecraft/world/item/crafting/Ingredient.of([Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/crafting/Ingredient;
static: true
newOwner: net/minecraft/world/item/crafting/Ingredient
uses: 2
usedIn: vectorwing/farmersdelight/client/gui/CookingPotRecipeBookComponent
shimClass: rose.era.v1_20_1.shim.IngredientShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/IngredientShim.java
shimSignature: public static net.minecraft.world.item.crafting.Ingredient of(net.minecraft.world.item.ItemStack[] itemStacks)
ruleLine: net/minecraft/world/item/crafting/Ingredient.m_43927_([Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/crafting/Ingredient;	rose/era/v1_20_1/shim/IngredientShim.of	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/crafting/Ingredient.of([Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/crafting/Ingredient;`

The mod calls **net/minecraft/world/item/crafting/Ingredient.of([Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/crafting/Ingredient;** (2 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `IngredientShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/IngredientShim.java`
- Signature: `public static net.minecraft.world.item.crafting.Ingredient of(net.minecraft.world.item.ItemStack[] itemStacks)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/crafting/Ingredient.m_43927_([Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/crafting/Ingredient;<TAB>rose/era/v1_20_1/shim/IngredientShim.of<TAB><evidence>`
