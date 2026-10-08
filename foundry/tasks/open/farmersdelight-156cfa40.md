---
id: farmersdelight-156cfa40
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/item/ItemStack.m_41739_(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;
readable: net/minecraft/world/item/ItemStack.save(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;
static: false
newOwner: net/minecraft/world/item/ItemStack
uses: 2
usedIn: vectorwing/farmersdelight/common/block/entity/SkilletBlockEntity
shimClass: rose.era.v1_20_1.shim.ItemStackShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemStackShim.java
shimSignature: public static net.minecraft.nbt.CompoundTag save(net.minecraft.world.item.ItemStack self, net.minecraft.nbt.CompoundTag compoundTag)
ruleLine: net/minecraft/world/item/ItemStack.m_41739_(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;	rose/era/v1_20_1/shim/ItemStackShim.save	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/ItemStack.save(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;`

The mod calls **net/minecraft/world/item/ItemStack.save(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;** (2 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `ItemStackShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemStackShim.java`
- Signature: `public static net.minecraft.nbt.CompoundTag save(net.minecraft.world.item.ItemStack self, net.minecraft.nbt.CompoundTag compoundTag)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/ItemStack.m_41739_(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;<TAB>rose/era/v1_20_1/shim/ItemStackShim.save<TAB><evidence>`
