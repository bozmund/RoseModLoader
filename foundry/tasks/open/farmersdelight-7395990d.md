---
id: farmersdelight-7395990d
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/item/crafting/RecipeManager.m_44043_(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;
readable: net/minecraft/world/item/crafting/RecipeManager.byKey(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;
static: false
newOwner: net/minecraft/world/item/crafting/RecipeManager
uses: 3
usedIn: vectorwing/farmersdelight/common/block/entity/CookingPotBlockEntity,vectorwing/farmersdelight/integration/emi/EMIPlugin,vectorwing/farmersdelight/integration/jei/FDRecipes
shimClass: rose.era.v1_20_1.shim.RecipeManagerShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/RecipeManagerShim.java
shimSignature: public static java.util.Optional byKey(net.minecraft.world.item.crafting.RecipeManager self, net.minecraft.resources.Identifier identifier)
ruleLine: net/minecraft/world/item/crafting/RecipeManager.m_44043_(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;	rose/era/v1_20_1/shim/RecipeManagerShim.byKey	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/crafting/RecipeManager.byKey(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;`

The mod calls **net/minecraft/world/item/crafting/RecipeManager.byKey(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;** (3 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `RecipeManagerShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/RecipeManagerShim.java`
- Signature: `public static java.util.Optional byKey(net.minecraft.world.item.crafting.RecipeManager self, net.minecraft.resources.Identifier identifier)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/crafting/RecipeManager.m_44043_(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;<TAB>rose/era/v1_20_1/shim/RecipeManagerShim.byKey<TAB><evidence>`
