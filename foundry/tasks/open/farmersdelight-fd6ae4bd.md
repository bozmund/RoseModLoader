---
id: farmersdelight-fd6ae4bd
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/level/block/BaseEntityBlock.m_7397_(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/item/ItemStack;
readable: net/minecraft/world/level/block/BaseEntityBlock.getCloneItemStack(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/item/ItemStack;
static: false
newOwner: net/minecraft/world/level/block/BaseEntityBlock
uses: 1
usedIn: vectorwing/farmersdelight/common/block/SkilletBlock
shimClass: rose.era.v1_20_1.shim.BaseEntityBlockShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BaseEntityBlockShim.java
shimSignature: public static net.minecraft.world.item.ItemStack getCloneItemStack(net.minecraft.world.level.block.BaseEntityBlock self, net.minecraft.world.level.BlockGetter blockGetter, net.minecraft.core.BlockPos blockPos, net.minecraft.world.level.block.state.BlockState blockState)
ruleLine: net/minecraft/world/level/block/BaseEntityBlock.m_7397_(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/item/ItemStack;	rose/era/v1_20_1/shim/BaseEntityBlockShim.getCloneItemStack	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/level/block/BaseEntityBlock.getCloneItemStack(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/item/ItemStack;`

The mod calls **net/minecraft/world/level/block/BaseEntityBlock.getCloneItemStack(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/item/ItemStack;** (1 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `BaseEntityBlockShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BaseEntityBlockShim.java`
- Signature: `public static net.minecraft.world.item.ItemStack getCloneItemStack(net.minecraft.world.level.block.BaseEntityBlock self, net.minecraft.world.level.BlockGetter blockGetter, net.minecraft.core.BlockPos blockPos, net.minecraft.world.level.block.state.BlockState blockState)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/level/block/BaseEntityBlock.m_7397_(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/item/ItemStack;<TAB>rose/era/v1_20_1/shim/BaseEntityBlockShim.getCloneItemStack<TAB><evidence>`
