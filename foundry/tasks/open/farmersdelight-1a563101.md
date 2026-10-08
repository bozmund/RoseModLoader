---
id: farmersdelight-1a563101
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/nbt/CompoundTag.m_128471_(Ljava/lang/String;)Z
readable: net/minecraft/nbt/CompoundTag.getBoolean(Ljava/lang/String;)Z
static: false
newOwner: net/minecraft/nbt/CompoundTag
uses: 4
usedIn: vectorwing/farmersdelight/client/renderer/SkilletItemRenderer,vectorwing/farmersdelight/common/block/entity/CuttingBoardBlockEntity,vectorwing/farmersdelight/common/item/SkilletItem
shimClass: rose.era.v1_20_1.shim.CompoundTagShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/CompoundTagShim.java
shimSignature: public static boolean getBoolean(net.minecraft.nbt.CompoundTag self, java.lang.String string)
ruleLine: net/minecraft/nbt/CompoundTag.m_128471_(Ljava/lang/String;)Z	rose/era/v1_20_1/shim/CompoundTagShim.getBoolean	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/nbt/CompoundTag.getBoolean(Ljava/lang/String;)Z`

The mod calls **net/minecraft/nbt/CompoundTag.getBoolean(Ljava/lang/String;)Z** (4 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `CompoundTagShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/CompoundTagShim.java`
- Signature: `public static boolean getBoolean(net.minecraft.nbt.CompoundTag self, java.lang.String string)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/nbt/CompoundTag.m_128471_(Ljava/lang/String;)Z<TAB>rose/era/v1_20_1/shim/CompoundTagShim.getBoolean<TAB><evidence>`
