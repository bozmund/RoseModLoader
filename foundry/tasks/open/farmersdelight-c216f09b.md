---
id: farmersdelight-c216f09b
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/item/BlockItem.m_6832_(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z
readable: net/minecraft/world/item/BlockItem.isValidRepairItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z
static: false
newOwner: net/minecraft/world/item/BlockItem
uses: 1
usedIn: vectorwing/farmersdelight/common/item/SkilletItem
shimClass: rose.era.v1_20_1.shim.BlockItemShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockItemShim.java
shimSignature: public static boolean isValidRepairItem(net.minecraft.world.item.BlockItem self, net.minecraft.world.item.ItemStack itemStack, net.minecraft.world.item.ItemStack itemStack2)
ruleLine: net/minecraft/world/item/BlockItem.m_6832_(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z	rose/era/v1_20_1/shim/BlockItemShim.isValidRepairItem	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/BlockItem.isValidRepairItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z`

The mod calls **net/minecraft/world/item/BlockItem.isValidRepairItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `BlockItemShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockItemShim.java`
- Signature: `public static boolean isValidRepairItem(net.minecraft.world.item.BlockItem self, net.minecraft.world.item.ItemStack itemStack, net.minecraft.world.item.ItemStack itemStack2)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/BlockItem.m_6832_(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z<TAB>rose/era/v1_20_1/shim/BlockItemShim.isValidRepairItem<TAB><evidence>`
