---
id: farmersdelight-5e66d88e
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/item/crafting/Recipe.m_5818_(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Z
readable: net/minecraft/world/item/crafting/Recipe.matches(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Z
static: false
newOwner: net/minecraft/world/item/crafting/Recipe
uses: 4
usedIn: vectorwing/farmersdelight/common/block/entity/CookingPotBlockEntity,vectorwing/farmersdelight/common/block/entity/CuttingBoardBlockEntity,vectorwing/farmersdelight/common/block/entity/SkilletBlockEntity,vectorwing/farmersdelight/common/block/entity/container/CookingPotMenu
shimClass: rose.era.v1_20_1.shim.RecipeShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/RecipeShim.java
shimSignature: public static boolean matches(net.minecraft.world.item.crafting.Recipe self, net.minecraft.world.Container container, net.minecraft.world.level.Level level)
ruleLine: net/minecraft/world/item/crafting/Recipe.m_5818_(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Z	rose/era/v1_20_1/shim/RecipeShim.matches	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/crafting/Recipe.matches(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Z`

The mod calls **net/minecraft/world/item/crafting/Recipe.matches(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Z** (4 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `RecipeShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/RecipeShim.java`
- Signature: `public static boolean matches(net.minecraft.world.item.crafting.Recipe self, net.minecraft.world.Container container, net.minecraft.world.level.Level level)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/crafting/Recipe.m_5818_(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Z<TAB>rose/era/v1_20_1/shim/RecipeShim.matches<TAB><evidence>`
