---
id: farmersdelight-b79df2de
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/ContainerHelper.m_18973_(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/NonNullList;)Lnet/minecraft/nbt/CompoundTag;
readable: net/minecraft/world/ContainerHelper.saveAllItems(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/NonNullList;)Lnet/minecraft/nbt/CompoundTag;
static: true
newOwner: net/minecraft/world/ContainerHelper
uses: 2
usedIn: vectorwing/farmersdelight/common/block/entity/BasketBlockEntity,vectorwing/farmersdelight/common/block/entity/CabinetBlockEntity
shimClass: rose.era.v1_20_1.shim.ContainerHelperShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ContainerHelperShim.java
shimSignature: public static net.minecraft.nbt.CompoundTag saveAllItems(net.minecraft.nbt.CompoundTag compoundTag, net.minecraft.core.NonNullList nonNullList)
ruleLine: net/minecraft/world/ContainerHelper.m_18973_(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/NonNullList;)Lnet/minecraft/nbt/CompoundTag;	rose/era/v1_20_1/shim/ContainerHelperShim.saveAllItems	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/ContainerHelper.saveAllItems(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/NonNullList;)Lnet/minecraft/nbt/CompoundTag;`

The mod calls **net/minecraft/world/ContainerHelper.saveAllItems(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/NonNullList;)Lnet/minecraft/nbt/CompoundTag;** (2 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `ContainerHelperShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ContainerHelperShim.java`
- Signature: `public static net.minecraft.nbt.CompoundTag saveAllItems(net.minecraft.nbt.CompoundTag compoundTag, net.minecraft.core.NonNullList nonNullList)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/ContainerHelper.m_18973_(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/NonNullList;)Lnet/minecraft/nbt/CompoundTag;<TAB>rose/era/v1_20_1/shim/ContainerHelperShim.saveAllItems<TAB><evidence>`
