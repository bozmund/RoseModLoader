---
id: farmersdelight-e15c0a30
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/item/ItemStack.m_220157_(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z
readable: net/minecraft/world/item/ItemStack.hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z
static: false
newOwner: net/minecraft/world/item/ItemStack
uses: 1
usedIn: vectorwing/farmersdelight/common/block/entity/CuttingBoardBlockEntity
shimClass: rose.era.v1_20_1.shim.ItemStackShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemStackShim.java
shimSignature: public static boolean hurt(net.minecraft.world.item.ItemStack self, int iValue, net.minecraft.util.RandomSource randomSource, net.minecraft.server.level.ServerPlayer serverPlayer)
ruleLine: net/minecraft/world/item/ItemStack.m_220157_(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z	rose/era/v1_20_1/shim/ItemStackShim.hurt	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/ItemStack.hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z`

The mod calls **net/minecraft/world/item/ItemStack.hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z** (1 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `ItemStackShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemStackShim.java`
- Signature: `public static boolean hurt(net.minecraft.world.item.ItemStack self, int iValue, net.minecraft.util.RandomSource randomSource, net.minecraft.server.level.ServerPlayer serverPlayer)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/ItemStack.m_220157_(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z<TAB>rose/era/v1_20_1/shim/ItemStackShim.hurt<TAB><evidence>`
