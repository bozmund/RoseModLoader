---
id: farmersdelight-80151f91
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/item/crafting/RecipeManager.m_44013_(Lnet/minecraft/world/item/crafting/RecipeType;)Ljava/util/List;
readable: net/minecraft/world/item/crafting/RecipeManager.getAllRecipesFor(Lnet/minecraft/world/item/crafting/RecipeType;)Ljava/util/List;
static: false
newOwner: net/minecraft/world/item/crafting/RecipeManager
uses: 5
usedIn: vectorwing/farmersdelight/common/loot/function/SmokerCookFunction,vectorwing/farmersdelight/integration/emi/EMIPlugin,vectorwing/farmersdelight/integration/jei/FDRecipes
shimClass: rose.era.v1_20_1.shim.RecipeManagerShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/RecipeManagerShim.java
shimSignature: public static java.util.List getAllRecipesFor(net.minecraft.world.item.crafting.RecipeManager self, net.minecraft.world.item.crafting.RecipeType recipeType)
ruleLine: net/minecraft/world/item/crafting/RecipeManager.m_44013_(Lnet/minecraft/world/item/crafting/RecipeType;)Ljava/util/List;	rose/era/v1_20_1/shim/RecipeManagerShim.getAllRecipesFor	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/crafting/RecipeManager.getAllRecipesFor(Lnet/minecraft/world/item/crafting/RecipeType;)Ljava/util/List;`

The mod calls **net/minecraft/world/item/crafting/RecipeManager.getAllRecipesFor(Lnet/minecraft/world/item/crafting/RecipeType;)Ljava/util/List;** (5 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `RecipeManagerShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/RecipeManagerShim.java`
- Signature: `public static java.util.List getAllRecipesFor(net.minecraft.world.item.crafting.RecipeManager self, net.minecraft.world.item.crafting.RecipeType recipeType)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/crafting/RecipeManager.m_44013_(Lnet/minecraft/world/item/crafting/RecipeType;)Ljava/util/List;<TAB>rose/era/v1_20_1/shim/RecipeManagerShim.getAllRecipesFor<TAB><evidence>`
