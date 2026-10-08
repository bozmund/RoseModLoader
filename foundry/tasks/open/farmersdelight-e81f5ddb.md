---
id: farmersdelight-e81f5ddb
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/nbt/CompoundTag.m_128473_(Ljava/lang/String;)V
readable: net/minecraft/nbt/CompoundTag.remove(Ljava/lang/String;)V
static: false
newOwner: net/minecraft/nbt/CompoundTag
uses: 6
usedIn: vectorwing/farmersdelight/common/block/entity/CookingPotBlockEntity,vectorwing/farmersdelight/common/item/SkilletItem
shimClass: rose.era.v1_20_1.shim.CompoundTagShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/CompoundTagShim.java
shimSignature: public static void remove(net.minecraft.nbt.CompoundTag self, java.lang.String string)
ruleLine: net/minecraft/nbt/CompoundTag.m_128473_(Ljava/lang/String;)V	rose/era/v1_20_1/shim/CompoundTagShim.remove	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/nbt/CompoundTag.remove(Ljava/lang/String;)V`

The mod calls **net/minecraft/nbt/CompoundTag.remove(Ljava/lang/String;)V** (6 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `CompoundTagShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/CompoundTagShim.java`
- Signature: `public static void remove(net.minecraft.nbt.CompoundTag self, java.lang.String string)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/nbt/CompoundTag.m_128473_(Ljava/lang/String;)V<TAB>rose/era/v1_20_1/shim/CompoundTagShim.remove<TAB><evidence>`
