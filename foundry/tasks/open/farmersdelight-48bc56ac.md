---
id: farmersdelight-48bc56ac
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/item/crafting/AbstractCookingRecipe.m_5874_(Lnet/minecraft/world/Container;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;
readable: net/minecraft/world/item/crafting/AbstractCookingRecipe.assemble(Lnet/minecraft/world/Container;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;
static: false
newOwner: net/minecraft/world/item/crafting/AbstractCookingRecipe
uses: 1
usedIn: vectorwing/farmersdelight/common/block/entity/AbstractStoveBlockEntity
shimClass: rose.era.v1_20_1.shim.AbstractCookingRecipeShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/AbstractCookingRecipeShim.java
shimSignature: public static net.minecraft.world.item.ItemStack assemble(net.minecraft.world.item.crafting.AbstractCookingRecipe self, net.minecraft.world.Container container, net.minecraft.core.RegistryAccess registryAccess)
ruleLine: net/minecraft/world/item/crafting/AbstractCookingRecipe.m_5874_(Lnet/minecraft/world/Container;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;	rose/era/v1_20_1/shim/AbstractCookingRecipeShim.assemble	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/crafting/AbstractCookingRecipe.assemble(Lnet/minecraft/world/Container;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;`

The mod calls **net/minecraft/world/item/crafting/AbstractCookingRecipe.assemble(Lnet/minecraft/world/Container;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;** (1 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `AbstractCookingRecipeShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/AbstractCookingRecipeShim.java`
- Signature: `public static net.minecraft.world.item.ItemStack assemble(net.minecraft.world.item.crafting.AbstractCookingRecipe self, net.minecraft.world.Container container, net.minecraft.core.RegistryAccess registryAccess)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/crafting/AbstractCookingRecipe.m_5874_(Lnet/minecraft/world/Container;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;<TAB>rose/era/v1_20_1/shim/AbstractCookingRecipeShim.assemble<TAB><evidence>`
