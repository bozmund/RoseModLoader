---
id: farmersdelight-5a3ce7be
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/nbt/CompoundTag.m_128425_(Ljava/lang/String;I)Z
readable: net/minecraft/nbt/CompoundTag.contains(Ljava/lang/String;I)Z
static: false
newOwner: net/minecraft/nbt/CompoundTag
uses: 5
usedIn: vectorwing/farmersdelight/common/block/entity/AbstractStoveBlockEntity,vectorwing/farmersdelight/common/block/entity/CookingPotBlockEntity
shimClass: rose.era.v1_20_1.shim.CompoundTagShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/CompoundTagShim.java
shimSignature: public static boolean contains(net.minecraft.nbt.CompoundTag self, java.lang.String string, int iValue)
ruleLine: net/minecraft/nbt/CompoundTag.m_128425_(Ljava/lang/String;I)Z	rose/era/v1_20_1/shim/CompoundTagShim.contains	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/nbt/CompoundTag.contains(Ljava/lang/String;I)Z`

The mod calls **net/minecraft/nbt/CompoundTag.contains(Ljava/lang/String;I)Z** (5 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `CompoundTagShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/CompoundTagShim.java`
- Signature: `public static boolean contains(net.minecraft.nbt.CompoundTag self, java.lang.String string, int iValue)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/nbt/CompoundTag.m_128425_(Ljava/lang/String;I)Z<TAB>rose/era/v1_20_1/shim/CompoundTagShim.contains<TAB><evidence>`
