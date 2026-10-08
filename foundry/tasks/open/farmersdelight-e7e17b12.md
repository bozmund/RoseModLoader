---
id: farmersdelight-e7e17b12
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/inventory/RecipeBookMenu.m_6656_()I
readable: net/minecraft/world/inventory/RecipeBookMenu.getGridHeight()I
static: false
newOwner: net/minecraft/world/inventory/RecipeBookMenu
uses: 1
usedIn: vectorwing/farmersdelight/client/gui/CookingPotRecipeBookComponent
shimClass: rose.era.v1_20_1.shim.RecipeBookMenuShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/RecipeBookMenuShim.java
shimSignature: public static int getGridHeight(net.minecraft.world.inventory.RecipeBookMenu self)
ruleLine: net/minecraft/world/inventory/RecipeBookMenu.m_6656_()I	rose/era/v1_20_1/shim/RecipeBookMenuShim.getGridHeight	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/inventory/RecipeBookMenu.getGridHeight()I`

The mod calls **net/minecraft/world/inventory/RecipeBookMenu.getGridHeight()I** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `RecipeBookMenuShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/RecipeBookMenuShim.java`
- Signature: `public static int getGridHeight(net.minecraft.world.inventory.RecipeBookMenu self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/inventory/RecipeBookMenu.m_6656_()I<TAB>rose/era/v1_20_1/shim/RecipeBookMenuShim.getGridHeight<TAB><evidence>`
