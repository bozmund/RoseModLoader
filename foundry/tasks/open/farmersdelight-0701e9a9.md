---
id: farmersdelight-0701e9a9
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/nbt/CompoundTag.m_128465_(Ljava/lang/String;)[I
readable: net/minecraft/nbt/CompoundTag.getIntArray(Ljava/lang/String;)[I
static: false
newOwner: net/minecraft/nbt/CompoundTag
uses: 2
usedIn: vectorwing/farmersdelight/common/block/entity/AbstractStoveBlockEntity
shimClass: rose.era.v1_20_1.shim.CompoundTagShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/CompoundTagShim.java
shimSignature: public static int[] getIntArray(net.minecraft.nbt.CompoundTag self, java.lang.String string)
ruleLine: net/minecraft/nbt/CompoundTag.m_128465_(Ljava/lang/String;)[I	rose/era/v1_20_1/shim/CompoundTagShim.getIntArray	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/nbt/CompoundTag.getIntArray(Ljava/lang/String;)[I`

The mod calls **net/minecraft/nbt/CompoundTag.getIntArray(Ljava/lang/String;)[I** (2 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `CompoundTagShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/CompoundTagShim.java`
- Signature: `public static int[] getIntArray(net.minecraft.nbt.CompoundTag self, java.lang.String string)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/nbt/CompoundTag.m_128465_(Ljava/lang/String;)[I<TAB>rose/era/v1_20_1/shim/CompoundTagShim.getIntArray<TAB><evidence>`
