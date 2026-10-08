---
id: farmersdelight-d43d07a2
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/item/crafting/RecipeManager$CachedCheck.m_213657_(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;
readable: net/minecraft/world/item/crafting/RecipeManager$CachedCheck.getRecipeFor(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;
static: false
newOwner: net/minecraft/world/item/crafting/RecipeManager$CachedCheck
uses: 2
usedIn: vectorwing/farmersdelight/common/block/entity/AbstractStoveBlockEntity
shimClass: rose.era.v1_20_1.shim.RecipeManagerCachedCheckShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/RecipeManagerCachedCheckShim.java
shimSignature: public static java.util.Optional getRecipeFor(net.minecraft.world.item.crafting.RecipeManager.CachedCheck self, net.minecraft.world.Container container, net.minecraft.world.level.Level level)
ruleLine: net/minecraft/world/item/crafting/RecipeManager$CachedCheck.m_213657_(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;	rose/era/v1_20_1/shim/RecipeManagerCachedCheckShim.getRecipeFor	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/crafting/RecipeManager$CachedCheck.getRecipeFor(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;`

The mod calls **net/minecraft/world/item/crafting/RecipeManager$CachedCheck.getRecipeFor(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;** (2 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `RecipeManagerCachedCheckShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/RecipeManagerCachedCheckShim.java`
- Signature: `public static java.util.Optional getRecipeFor(net.minecraft.world.item.crafting.RecipeManager.CachedCheck self, net.minecraft.world.Container container, net.minecraft.world.level.Level level)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/crafting/RecipeManager$CachedCheck.m_213657_(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;<TAB>rose/era/v1_20_1/shim/RecipeManagerCachedCheckShim.getRecipeFor<TAB><evidence>`
