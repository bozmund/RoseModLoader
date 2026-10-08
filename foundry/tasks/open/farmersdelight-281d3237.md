---
id: farmersdelight-281d3237
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/nbt/CompoundTag.m_128451_(Ljava/lang/String;)I
readable: net/minecraft/nbt/CompoundTag.getInt(Ljava/lang/String;)I
static: false
newOwner: net/minecraft/nbt/CompoundTag
uses: 8
usedIn: vectorwing/farmersdelight/common/block/entity/BasketBlockEntity,vectorwing/farmersdelight/common/block/entity/CookingPotBlockEntity,vectorwing/farmersdelight/common/block/entity/SkilletBlockEntity,vectorwing/farmersdelight/common/item/SkilletItem,vectorwing/farmersdelight/data/tools/StructureUpdater
shimClass: rose.era.v1_20_1.shim.CompoundTagShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/CompoundTagShim.java
shimSignature: public static int getInt(net.minecraft.nbt.CompoundTag self, java.lang.String string)
ruleLine: net/minecraft/nbt/CompoundTag.m_128451_(Ljava/lang/String;)I	rose/era/v1_20_1/shim/CompoundTagShim.getInt	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/nbt/CompoundTag.getInt(Ljava/lang/String;)I`

The mod calls **net/minecraft/nbt/CompoundTag.getInt(Ljava/lang/String;)I** (8 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `CompoundTagShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/CompoundTagShim.java`
- Signature: `public static int getInt(net.minecraft.nbt.CompoundTag self, java.lang.String string)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/nbt/CompoundTag.m_128451_(Ljava/lang/String;)I<TAB>rose/era/v1_20_1/shim/CompoundTagShim.getInt<TAB><evidence>`
