---
id: farmersdelight-ec97a2b3
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/item/ItemStack.m_41737_(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;
readable: net/minecraft/world/item/ItemStack.getTagElement(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;
static: false
newOwner: net/minecraft/world/item/ItemStack
uses: 9
usedIn: vectorwing/farmersdelight/client/ClientSetup,vectorwing/farmersdelight/client/renderer/SkilletItemRenderer,vectorwing/farmersdelight/common/block/entity/CookingPotBlockEntity,vectorwing/farmersdelight/common/item/CookingPotItem,vectorwing/farmersdelight/common/item/SkilletItem
shimClass: rose.era.v1_20_1.shim.ItemStackShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemStackShim.java
shimSignature: public static net.minecraft.nbt.CompoundTag getTagElement(net.minecraft.world.item.ItemStack self, java.lang.String string)
ruleLine: net/minecraft/world/item/ItemStack.m_41737_(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;	rose/era/v1_20_1/shim/ItemStackShim.getTagElement	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/ItemStack.getTagElement(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;`

The mod calls **net/minecraft/world/item/ItemStack.getTagElement(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;** (9 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `ItemStackShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemStackShim.java`
- Signature: `public static net.minecraft.nbt.CompoundTag getTagElement(net.minecraft.world.item.ItemStack self, java.lang.String string)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/ItemStack.m_41737_(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;<TAB>rose/era/v1_20_1/shim/ItemStackShim.getTagElement<TAB><evidence>`
