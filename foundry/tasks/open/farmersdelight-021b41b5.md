---
id: farmersdelight-021b41b5
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/item/ItemStack.m_41678_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;I)V
readable: net/minecraft/world/item/ItemStack.onCraftedBy(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;I)V
static: false
newOwner: net/minecraft/world/item/ItemStack
uses: 1
usedIn: vectorwing/farmersdelight/common/block/entity/container/CookingPotResultSlot
shimClass: rose.era.v1_20_1.shim.ItemStackShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemStackShim.java
shimSignature: public static void onCraftedBy(net.minecraft.world.item.ItemStack self, net.minecraft.world.level.Level level, net.minecraft.world.entity.player.Player player, int iValue)
ruleLine: net/minecraft/world/item/ItemStack.m_41678_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;I)V	rose/era/v1_20_1/shim/ItemStackShim.onCraftedBy	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/ItemStack.onCraftedBy(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;I)V`

The mod calls **net/minecraft/world/item/ItemStack.onCraftedBy(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;I)V** (1 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `ItemStackShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemStackShim.java`
- Signature: `public static void onCraftedBy(net.minecraft.world.item.ItemStack self, net.minecraft.world.level.Level level, net.minecraft.world.entity.player.Player player, int iValue)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/ItemStack.m_41678_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;I)V<TAB>rose/era/v1_20_1/shim/ItemStackShim.onCraftedBy<TAB><evidence>`
