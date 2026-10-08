---
id: farmersdelight-520c3789
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/level/block/entity/BlockEntity.m_5995_()Lnet/minecraft/nbt/CompoundTag;
readable: net/minecraft/world/level/block/entity/BlockEntity.getUpdateTag()Lnet/minecraft/nbt/CompoundTag;
static: false
newOwner: net/minecraft/world/level/block/entity/BlockEntity
uses: 1
usedIn: vectorwing/farmersdelight/common/block/entity/AbstractStoveBlockEntity
shimClass: rose.era.v1_20_1.shim.BlockEntityShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockEntityShim.java
shimSignature: public static net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.world.level.block.entity.BlockEntity self)
ruleLine: net/minecraft/world/level/block/entity/BlockEntity.m_5995_()Lnet/minecraft/nbt/CompoundTag;	rose/era/v1_20_1/shim/BlockEntityShim.getUpdateTag	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/level/block/entity/BlockEntity.getUpdateTag()Lnet/minecraft/nbt/CompoundTag;`

The mod calls **net/minecraft/world/level/block/entity/BlockEntity.getUpdateTag()Lnet/minecraft/nbt/CompoundTag;** (1 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `BlockEntityShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockEntityShim.java`
- Signature: `public static net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.world.level.block.entity.BlockEntity self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/level/block/entity/BlockEntity.m_5995_()Lnet/minecraft/nbt/CompoundTag;<TAB>rose/era/v1_20_1/shim/BlockEntityShim.getUpdateTag<TAB><evidence>`
