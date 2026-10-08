---
id: farmersdelight-23d12364
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/item/ItemStack.m_41782_()Z
readable: net/minecraft/world/item/ItemStack.hasTag()Z
static: false
newOwner: net/minecraft/world/item/ItemStack
uses: 1
usedIn: vectorwing/farmersdelight/common/crafting/ingredient/ChanceResult
shimClass: rose.era.v1_20_1.shim.ItemStackShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemStackShim.java
shimSignature: public static boolean hasTag(net.minecraft.world.item.ItemStack self)
ruleLine: net/minecraft/world/item/ItemStack.m_41782_()Z	rose/era/v1_20_1/shim/ItemStackShim.hasTag	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/ItemStack.hasTag()Z`

The mod calls **net/minecraft/world/item/ItemStack.hasTag()Z** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `ItemStackShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemStackShim.java`
- Signature: `public static boolean hasTag(net.minecraft.world.item.ItemStack self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/ItemStack.m_41782_()Z<TAB>rose/era/v1_20_1/shim/ItemStackShim.hasTag<TAB><evidence>`
