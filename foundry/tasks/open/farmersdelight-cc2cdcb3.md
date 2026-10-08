---
id: farmersdelight-cc2cdcb3
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/entity/player/Player.m_36176_(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;
readable: net/minecraft/world/entity/player/Player.drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;
static: false
newOwner: net/minecraft/world/entity/player/Player
uses: 5
usedIn: vectorwing/farmersdelight/common/block/CookingPotBlock,vectorwing/farmersdelight/common/block/FeastBlock,vectorwing/farmersdelight/common/item/ConsumableItem,vectorwing/farmersdelight/common/item/SkilletItem,vectorwing/farmersdelight/common/mixin/SoupItemMixin
shimClass: rose.era.v1_20_1.shim.PlayerShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/PlayerShim.java
shimSignature: public static net.minecraft.world.entity.item.ItemEntity drop(net.minecraft.world.entity.player.Player self, net.minecraft.world.item.ItemStack itemStack, boolean bValue)
ruleLine: net/minecraft/world/entity/player/Player.m_36176_(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;	rose/era/v1_20_1/shim/PlayerShim.drop	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/entity/player/Player.drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;`

The mod calls **net/minecraft/world/entity/player/Player.drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;** (5 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `PlayerShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/PlayerShim.java`
- Signature: `public static net.minecraft.world.entity.item.ItemEntity drop(net.minecraft.world.entity.player.Player self, net.minecraft.world.item.ItemStack itemStack, boolean bValue)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/entity/player/Player.m_36176_(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;<TAB>rose/era/v1_20_1/shim/PlayerShim.drop<TAB><evidence>`
