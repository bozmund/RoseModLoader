---
id: farmersdelight-46f5610a
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/item/ItemStack.m_41784_()Lnet/minecraft/nbt/CompoundTag;
readable: net/minecraft/world/item/ItemStack.getOrCreateTag()Lnet/minecraft/nbt/CompoundTag;
static: false
newOwner: net/minecraft/world/item/ItemStack
uses: 15
usedIn: vectorwing/farmersdelight/client/event/KeybindEvents,vectorwing/farmersdelight/client/renderer/SkilletItemRenderer,vectorwing/farmersdelight/client/renderer/SkilletItemRenderer$ArmPoseTransformer,vectorwing/farmersdelight/common/item/SkilletItem,vectorwing/farmersdelight/common/item/SkilletItem$1,vectorwing/farmersdelight/common/network/ModNetworking$FlipSkilletMessage
shimClass: rose.era.v1_20_1.shim.ItemStackShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemStackShim.java
shimSignature: public static net.minecraft.nbt.CompoundTag getOrCreateTag(net.minecraft.world.item.ItemStack self)
ruleLine: net/minecraft/world/item/ItemStack.m_41784_()Lnet/minecraft/nbt/CompoundTag;	rose/era/v1_20_1/shim/ItemStackShim.getOrCreateTag	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/ItemStack.getOrCreateTag()Lnet/minecraft/nbt/CompoundTag;`

The mod calls **net/minecraft/world/item/ItemStack.getOrCreateTag()Lnet/minecraft/nbt/CompoundTag;** (15 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `ItemStackShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemStackShim.java`
- Signature: `public static net.minecraft.nbt.CompoundTag getOrCreateTag(net.minecraft.world.item.ItemStack self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/ItemStack.m_41784_()Lnet/minecraft/nbt/CompoundTag;<TAB>rose/era/v1_20_1/shim/ItemStackShim.getOrCreateTag<TAB><evidence>`
