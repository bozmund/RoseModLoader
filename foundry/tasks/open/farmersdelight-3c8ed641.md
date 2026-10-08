---
id: farmersdelight-3c8ed641
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/nbt/TagParser.m_129359_(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;
readable: net/minecraft/nbt/TagParser.parseTag(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;
static: true
newOwner: net/minecraft/nbt/TagParser
uses: 1
usedIn: vectorwing/farmersdelight/common/crafting/ingredient/ChanceResult
shimClass: rose.era.v1_20_1.shim.TagParserShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/TagParserShim.java
shimSignature: public static net.minecraft.nbt.CompoundTag parseTag(java.lang.String string)
ruleLine: net/minecraft/nbt/TagParser.m_129359_(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;	rose/era/v1_20_1/shim/TagParserShim.parseTag	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/nbt/TagParser.parseTag(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;`

The mod calls **net/minecraft/nbt/TagParser.parseTag(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `TagParserShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/TagParserShim.java`
- Signature: `public static net.minecraft.nbt.CompoundTag parseTag(java.lang.String string)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/nbt/TagParser.m_129359_(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;<TAB>rose/era/v1_20_1/shim/TagParserShim.parseTag<TAB><evidence>`
