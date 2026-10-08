---
id: farmersdelight-4b3b2140
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/item/crafting/Ingredient.m_43923_(Lnet/minecraft/network/FriendlyByteBuf;)V
readable: net/minecraft/world/item/crafting/Ingredient.toNetwork(Lnet/minecraft/network/FriendlyByteBuf;)V
static: false
newOwner: net/minecraft/world/item/crafting/Ingredient
uses: 3
usedIn: vectorwing/farmersdelight/common/crafting/CookingPotRecipe$Serializer,vectorwing/farmersdelight/common/crafting/CuttingBoardRecipe$Serializer
shimClass: rose.era.v1_20_1.shim.IngredientShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/IngredientShim.java
shimSignature: public static void toNetwork(net.minecraft.world.item.crafting.Ingredient self, net.minecraft.network.FriendlyByteBuf friendlyByteBuf)
ruleLine: net/minecraft/world/item/crafting/Ingredient.m_43923_(Lnet/minecraft/network/FriendlyByteBuf;)V	rose/era/v1_20_1/shim/IngredientShim.toNetwork	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/crafting/Ingredient.toNetwork(Lnet/minecraft/network/FriendlyByteBuf;)V`

The mod calls **net/minecraft/world/item/crafting/Ingredient.toNetwork(Lnet/minecraft/network/FriendlyByteBuf;)V** (3 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `IngredientShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/IngredientShim.java`
- Signature: `public static void toNetwork(net.minecraft.world.item.crafting.Ingredient self, net.minecraft.network.FriendlyByteBuf friendlyByteBuf)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/crafting/Ingredient.m_43923_(Lnet/minecraft/network/FriendlyByteBuf;)V<TAB>rose/era/v1_20_1/shim/IngredientShim.toNetwork<TAB><evidence>`
