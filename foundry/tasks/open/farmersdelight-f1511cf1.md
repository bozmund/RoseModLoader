---
id: farmersdelight-f1511cf1
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/item/crafting/SmokingRecipe.m_7527_()Lnet/minecraft/core/NonNullList;
readable: net/minecraft/world/item/crafting/SmokingRecipe.getIngredients()Lnet/minecraft/core/NonNullList;
static: false
newOwner: net/minecraft/world/item/crafting/SmokingRecipe
uses: 1
usedIn: vectorwing/farmersdelight/common/loot/function/SmokerCookFunction
shimClass: rose.era.v1_20_1.shim.SmokingRecipeShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/SmokingRecipeShim.java
shimSignature: public static net.minecraft.core.NonNullList getIngredients(net.minecraft.world.item.crafting.SmokingRecipe self)
ruleLine: net/minecraft/world/item/crafting/SmokingRecipe.m_7527_()Lnet/minecraft/core/NonNullList;	rose/era/v1_20_1/shim/SmokingRecipeShim.getIngredients	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/crafting/SmokingRecipe.getIngredients()Lnet/minecraft/core/NonNullList;`

The mod calls **net/minecraft/world/item/crafting/SmokingRecipe.getIngredients()Lnet/minecraft/core/NonNullList;** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `SmokingRecipeShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/SmokingRecipeShim.java`
- Signature: `public static net.minecraft.core.NonNullList getIngredients(net.minecraft.world.item.crafting.SmokingRecipe self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/crafting/SmokingRecipe.m_7527_()Lnet/minecraft/core/NonNullList;<TAB>rose/era/v1_20_1/shim/SmokingRecipeShim.getIngredients<TAB><evidence>`
