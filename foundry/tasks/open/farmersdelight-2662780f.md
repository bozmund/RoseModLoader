---
id: farmersdelight-2662780f
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/item/crafting/Recipe.m_6423_()Lnet/minecraft/resources/ResourceLocation;
readable: net/minecraft/world/item/crafting/Recipe.getId()Lnet/minecraft/resources/ResourceLocation;
static: false
newOwner: net/minecraft/world/item/crafting/Recipe
uses: 2
usedIn: vectorwing/farmersdelight/common/block/entity/CookingPotBlockEntity,vectorwing/farmersdelight/integration/jei/FDRecipes
shimClass: rose.era.v1_20_1.shim.RecipeShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/RecipeShim.java
shimSignature: public static net.minecraft.resources.Identifier getId(net.minecraft.world.item.crafting.Recipe self)
ruleLine: net/minecraft/world/item/crafting/Recipe.m_6423_()Lnet/minecraft/resources/ResourceLocation;	rose/era/v1_20_1/shim/RecipeShim.getId	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/crafting/Recipe.getId()Lnet/minecraft/resources/ResourceLocation;`

The mod calls **net/minecraft/world/item/crafting/Recipe.getId()Lnet/minecraft/resources/ResourceLocation;** (2 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `RecipeShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/RecipeShim.java`
- Signature: `public static net.minecraft.resources.Identifier getId(net.minecraft.world.item.crafting.Recipe self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/crafting/Recipe.m_6423_()Lnet/minecraft/resources/ResourceLocation;<TAB>rose/era/v1_20_1/shim/RecipeShim.getId<TAB><evidence>`
