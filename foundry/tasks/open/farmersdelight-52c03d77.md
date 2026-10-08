---
id: farmersdelight-52c03d77
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/item/Item.m_41472_()Z
readable: net/minecraft/world/item/Item.isEdible()Z
static: false
newOwner: net/minecraft/world/item/Item
uses: 1
usedIn: vectorwing/farmersdelight/common/block/PieBlock
shimClass: rose.era.v1_20_1.shim.ItemShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemShim.java
shimSignature: public static boolean isEdible(net.minecraft.world.item.Item self)
ruleLine: net/minecraft/world/item/Item.m_41472_()Z	rose/era/v1_20_1/shim/ItemShim.isEdible	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/Item.isEdible()Z`

The mod calls **net/minecraft/world/item/Item.isEdible()Z** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `ItemShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemShim.java`
- Signature: `public static boolean isEdible(net.minecraft.world.item.Item self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/Item.m_41472_()Z<TAB>rose/era/v1_20_1/shim/ItemShim.isEdible<TAB><evidence>`
