---
id: farmersdelight-8e53edbf
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/nbt/CompoundTag.m_128461_(Ljava/lang/String;)Ljava/lang/String;
readable: net/minecraft/nbt/CompoundTag.getString(Ljava/lang/String;)Ljava/lang/String;
static: false
newOwner: net/minecraft/nbt/CompoundTag
uses: 1
usedIn: vectorwing/farmersdelight/common/block/entity/CookingPotBlockEntity
shimClass: rose.era.v1_20_1.shim.CompoundTagShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/CompoundTagShim.java
shimSignature: public static java.lang.String getString(net.minecraft.nbt.CompoundTag self, java.lang.String string)
ruleLine: net/minecraft/nbt/CompoundTag.m_128461_(Ljava/lang/String;)Ljava/lang/String;	rose/era/v1_20_1/shim/CompoundTagShim.getString	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/nbt/CompoundTag.getString(Ljava/lang/String;)Ljava/lang/String;`

The mod calls **net/minecraft/nbt/CompoundTag.getString(Ljava/lang/String;)Ljava/lang/String;** (1 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `CompoundTagShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/CompoundTagShim.java`
- Signature: `public static java.lang.String getString(net.minecraft.nbt.CompoundTag self, java.lang.String string)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/nbt/CompoundTag.m_128461_(Ljava/lang/String;)Ljava/lang/String;<TAB>rose/era/v1_20_1/shim/CompoundTagShim.getString<TAB><evidence>`
