---
id: farmersdelight-3d303ffd
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/level/Level.m_7465_()Lnet/minecraft/world/item/crafting/RecipeManager;
readable: net/minecraft/world/level/Level.getRecipeManager()Lnet/minecraft/world/item/crafting/RecipeManager;
static: false
newOwner: net/minecraft/world/level/Level
uses: 8
usedIn: vectorwing/farmersdelight/common/block/entity/CookingPotBlockEntity,vectorwing/farmersdelight/common/block/entity/CuttingBoardBlockEntity,vectorwing/farmersdelight/common/block/entity/SkilletBlockEntity,vectorwing/farmersdelight/common/item/SkilletItem
shimClass: rose.era.v1_20_1.shim.LevelShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/LevelShim.java
shimSignature: public static net.minecraft.world.item.crafting.RecipeManager getRecipeManager(net.minecraft.world.level.Level self)
ruleLine: net/minecraft/world/level/Level.m_7465_()Lnet/minecraft/world/item/crafting/RecipeManager;	rose/era/v1_20_1/shim/LevelShim.getRecipeManager	<evidence>
attempts: 0
created: 2026-10-09
closedReason: -
---

# Redirect `net/minecraft/world/level/Level.getRecipeManager()Lnet/minecraft/world/item/crafting/RecipeManager;`

The mod calls **net/minecraft/world/level/Level.getRecipeManager()Lnet/minecraft/world/item/crafting/RecipeManager;** (8 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `LevelShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/LevelShim.java`
- Signature: `public static net.minecraft.world.item.crafting.RecipeManager getRecipeManager(net.minecraft.world.level.Level self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/level/Level.m_7465_()Lnet/minecraft/world/item/crafting/RecipeManager;<TAB>rose/era/v1_20_1/shim/LevelShim.getRecipeManager<TAB><evidence>`

## Closed 2026-10-09 00:38

No longer reported by `rose analyze` (resolved elsewhere).

## Reopened 2026-10-09 00:39

Reported again by `rose analyze`.
