---
id: farmersdelight-9914fd77
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/nbt/CompoundTag.m_128469_(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;
readable: net/minecraft/nbt/CompoundTag.getCompound(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;
static: false
newOwner: net/minecraft/nbt/CompoundTag
uses: 14
usedIn: vectorwing/farmersdelight/common/block/SkilletBlock,vectorwing/farmersdelight/common/block/entity/AbstractStoveBlockEntity,vectorwing/farmersdelight/common/block/entity/CookingPotBlockEntity,vectorwing/farmersdelight/common/block/entity/CuttingBoardBlockEntity,vectorwing/farmersdelight/common/block/entity/SkilletBlockEntity,vectorwing/farmersdelight/common/item/SkilletItem,vectorwing/farmersdelight/common/loot/function/CopySkilletFunction
shimClass: rose.era.v1_20_1.shim.CompoundTagShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/CompoundTagShim.java
shimSignature: public static net.minecraft.nbt.CompoundTag getCompound(net.minecraft.nbt.CompoundTag self, java.lang.String string)
ruleLine: net/minecraft/nbt/CompoundTag.m_128469_(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;	rose/era/v1_20_1/shim/CompoundTagShim.getCompound	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/nbt/CompoundTag.getCompound(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;`

The mod calls **net/minecraft/nbt/CompoundTag.getCompound(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;** (14 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `CompoundTagShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/CompoundTagShim.java`
- Signature: `public static net.minecraft.nbt.CompoundTag getCompound(net.minecraft.nbt.CompoundTag self, java.lang.String string)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/nbt/CompoundTag.m_128469_(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;<TAB>rose/era/v1_20_1/shim/CompoundTagShim.getCompound<TAB><evidence>`
