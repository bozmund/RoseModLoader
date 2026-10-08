---
id: farmersdelight-9fa60d86
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/item/crafting/RecipeManager.m_44056_(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/List;
readable: net/minecraft/world/item/crafting/RecipeManager.getRecipesFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/List;
static: false
newOwner: net/minecraft/world/item/crafting/RecipeManager
uses: 1
usedIn: vectorwing/farmersdelight/common/block/entity/CuttingBoardBlockEntity
shimClass: rose.era.v1_20_1.shim.RecipeManagerShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/RecipeManagerShim.java
shimSignature: public static java.util.List getRecipesFor(net.minecraft.world.item.crafting.RecipeManager self, net.minecraft.world.item.crafting.RecipeType recipeType, net.minecraft.world.Container container, net.minecraft.world.level.Level level)
ruleLine: net/minecraft/world/item/crafting/RecipeManager.m_44056_(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/List;	rose/era/v1_20_1/shim/RecipeManagerShim.getRecipesFor	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/crafting/RecipeManager.getRecipesFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/List;`

The mod calls **net/minecraft/world/item/crafting/RecipeManager.getRecipesFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/List;** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `RecipeManagerShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/RecipeManagerShim.java`
- Signature: `public static java.util.List getRecipesFor(net.minecraft.world.item.crafting.RecipeManager self, net.minecraft.world.item.crafting.RecipeType recipeType, net.minecraft.world.Container container, net.minecraft.world.level.Level level)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/crafting/RecipeManager.m_44056_(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/List;<TAB>rose/era/v1_20_1/shim/RecipeManagerShim.getRecipesFor<TAB><evidence>`
