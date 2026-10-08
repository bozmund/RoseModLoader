---
id: farmersdelight-3afba3b7
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/item/ItemStack.m_41712_(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/item/ItemStack;
readable: net/minecraft/world/item/ItemStack.of(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/item/ItemStack;
static: true
newOwner: net/minecraft/world/item/ItemStack
uses: 8
usedIn: vectorwing/farmersdelight/client/renderer/SkilletItemRenderer,vectorwing/farmersdelight/common/block/SkilletBlock,vectorwing/farmersdelight/common/block/entity/CookingPotBlockEntity,vectorwing/farmersdelight/common/block/entity/SkilletBlockEntity,vectorwing/farmersdelight/common/item/SkilletItem,vectorwing/farmersdelight/common/loot/function/CopySkilletFunction
shimClass: rose.era.v1_20_1.shim.ItemStackShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemStackShim.java
shimSignature: public static net.minecraft.world.item.ItemStack of(net.minecraft.nbt.CompoundTag compoundTag)
ruleLine: net/minecraft/world/item/ItemStack.m_41712_(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/item/ItemStack;	rose/era/v1_20_1/shim/ItemStackShim.of	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/ItemStack.of(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/item/ItemStack;`

The mod calls **net/minecraft/world/item/ItemStack.of(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/item/ItemStack;** (8 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `ItemStackShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemStackShim.java`
- Signature: `public static net.minecraft.world.item.ItemStack of(net.minecraft.nbt.CompoundTag compoundTag)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/ItemStack.m_41712_(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/item/ItemStack;<TAB>rose/era/v1_20_1/shim/ItemStackShim.of<TAB><evidence>`
