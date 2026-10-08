---
id: farmersdelight-d32dd882
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/item/ItemStack.m_41614_()Z
readable: net/minecraft/world/item/ItemStack.isEdible()Z
static: false
newOwner: net/minecraft/world/item/ItemStack
uses: 3
usedIn: vectorwing/farmersdelight/common/item/ConsumableItem,vectorwing/farmersdelight/common/item/DrinkableItem,vectorwing/farmersdelight/common/mixin/SoupItemMixin
shimClass: rose.era.v1_20_1.shim.ItemStackShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemStackShim.java
shimSignature: public static boolean isEdible(net.minecraft.world.item.ItemStack self)
ruleLine: net/minecraft/world/item/ItemStack.m_41614_()Z	rose/era/v1_20_1/shim/ItemStackShim.isEdible	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/ItemStack.isEdible()Z`

The mod calls **net/minecraft/world/item/ItemStack.isEdible()Z** (3 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `ItemStackShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemStackShim.java`
- Signature: `public static boolean isEdible(net.minecraft.world.item.ItemStack self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/ItemStack.m_41614_()Z<TAB>rose/era/v1_20_1/shim/ItemStackShim.isEdible<TAB><evidence>`
