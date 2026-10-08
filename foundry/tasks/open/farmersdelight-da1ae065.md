---
id: farmersdelight-da1ae065
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/item/crafting/Recipe.m_8043_(Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;
readable: net/minecraft/world/item/crafting/Recipe.getResultItem(Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;
static: false
newOwner: net/minecraft/world/item/crafting/Recipe
uses: 3
usedIn: vectorwing/farmersdelight/client/gui/CookingPotRecipeBookComponent,vectorwing/farmersdelight/common/block/entity/CookingPotBlockEntity,vectorwing/farmersdelight/common/utility/RecipeUtils
shimClass: rose.era.v1_20_1.shim.RecipeShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/RecipeShim.java
shimSignature: public static net.minecraft.world.item.ItemStack getResultItem(net.minecraft.world.item.crafting.Recipe self, net.minecraft.core.RegistryAccess registryAccess)
ruleLine: net/minecraft/world/item/crafting/Recipe.m_8043_(Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;	rose/era/v1_20_1/shim/RecipeShim.getResultItem	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/crafting/Recipe.getResultItem(Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;`

The mod calls **net/minecraft/world/item/crafting/Recipe.getResultItem(Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;** (3 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `RecipeShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/RecipeShim.java`
- Signature: `public static net.minecraft.world.item.ItemStack getResultItem(net.minecraft.world.item.crafting.Recipe self, net.minecraft.core.RegistryAccess registryAccess)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/crafting/Recipe.m_8043_(Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;<TAB>rose/era/v1_20_1/shim/RecipeShim.getResultItem<TAB><evidence>`
