---
id: farmersdelight-08c45768
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/item/crafting/Ingredient.m_43917_(Lcom/google/gson/JsonElement;)Lnet/minecraft/world/item/crafting/Ingredient;
readable: net/minecraft/world/item/crafting/Ingredient.fromJson(Lcom/google/gson/JsonElement;)Lnet/minecraft/world/item/crafting/Ingredient;
static: true
newOwner: net/minecraft/world/item/crafting/Ingredient
uses: 3
usedIn: vectorwing/farmersdelight/common/crafting/CookingPotRecipe$Serializer,vectorwing/farmersdelight/common/crafting/CuttingBoardRecipe$Serializer
shimClass: rose.era.v1_20_1.shim.IngredientShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/IngredientShim.java
shimSignature: public static net.minecraft.world.item.crafting.Ingredient fromJson(com.google.gson.JsonElement jsonElement)
ruleLine: net/minecraft/world/item/crafting/Ingredient.m_43917_(Lcom/google/gson/JsonElement;)Lnet/minecraft/world/item/crafting/Ingredient;	rose/era/v1_20_1/shim/IngredientShim.fromJson	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/crafting/Ingredient.fromJson(Lcom/google/gson/JsonElement;)Lnet/minecraft/world/item/crafting/Ingredient;`

The mod calls **net/minecraft/world/item/crafting/Ingredient.fromJson(Lcom/google/gson/JsonElement;)Lnet/minecraft/world/item/crafting/Ingredient;** (3 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `IngredientShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/IngredientShim.java`
- Signature: `public static net.minecraft.world.item.crafting.Ingredient fromJson(com.google.gson.JsonElement jsonElement)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/crafting/Ingredient.m_43917_(Lcom/google/gson/JsonElement;)Lnet/minecraft/world/item/crafting/Ingredient;<TAB>rose/era/v1_20_1/shim/IngredientShim.fromJson<TAB><evidence>`
