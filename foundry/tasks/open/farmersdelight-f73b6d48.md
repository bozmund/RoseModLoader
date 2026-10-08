---
id: farmersdelight-f73b6d48
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/entity/player/Inventory.m_150079_(Lnet/minecraft/world/item/ItemStack;)V
readable: net/minecraft/world/entity/player/Inventory.placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;)V
static: false
newOwner: net/minecraft/world/entity/player/Inventory
uses: 1
usedIn: vectorwing/farmersdelight/common/item/SkilletItem
shimClass: rose.era.v1_20_1.shim.InventoryShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/InventoryShim.java
shimSignature: public static void placeItemBackInInventory(net.minecraft.world.entity.player.Inventory self, net.minecraft.world.item.ItemStack itemStack)
ruleLine: net/minecraft/world/entity/player/Inventory.m_150079_(Lnet/minecraft/world/item/ItemStack;)V	rose/era/v1_20_1/shim/InventoryShim.placeItemBackInInventory	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/entity/player/Inventory.placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;)V`

The mod calls **net/minecraft/world/entity/player/Inventory.placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;)V** (1 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `InventoryShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/InventoryShim.java`
- Signature: `public static void placeItemBackInInventory(net.minecraft.world.entity.player.Inventory self, net.minecraft.world.item.ItemStack itemStack)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/entity/player/Inventory.m_150079_(Lnet/minecraft/world/item/ItemStack;)V<TAB>rose/era/v1_20_1/shim/InventoryShim.placeItemBackInInventory<TAB><evidence>`
