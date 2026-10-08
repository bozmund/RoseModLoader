---
id: farmersdelight-b3c4789e
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/item/BlockItem.m_7274_(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;)Z
readable: net/minecraft/world/item/BlockItem.updateCustomBlockEntityTag(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;)Z
static: false
newOwner: net/minecraft/world/item/BlockItem
uses: 1
usedIn: vectorwing/farmersdelight/common/item/SkilletItem
shimClass: rose.era.v1_20_1.shim.BlockItemShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockItemShim.java
shimSignature: public static boolean updateCustomBlockEntityTag(net.minecraft.world.item.BlockItem self, net.minecraft.core.BlockPos blockPos, net.minecraft.world.level.Level level, net.minecraft.world.entity.player.Player player, net.minecraft.world.item.ItemStack itemStack, net.minecraft.world.level.block.state.BlockState blockState)
ruleLine: net/minecraft/world/item/BlockItem.m_7274_(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;)Z	rose/era/v1_20_1/shim/BlockItemShim.updateCustomBlockEntityTag	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/BlockItem.updateCustomBlockEntityTag(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;)Z`

The mod calls **net/minecraft/world/item/BlockItem.updateCustomBlockEntityTag(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;)Z** (1 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `BlockItemShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockItemShim.java`
- Signature: `public static boolean updateCustomBlockEntityTag(net.minecraft.world.item.BlockItem self, net.minecraft.core.BlockPos blockPos, net.minecraft.world.level.Level level, net.minecraft.world.entity.player.Player player, net.minecraft.world.item.ItemStack itemStack, net.minecraft.world.level.block.state.BlockState blockState)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/BlockItem.m_7274_(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;)Z<TAB>rose/era/v1_20_1/shim/BlockItemShim.updateCustomBlockEntityTag<TAB><evidence>`
