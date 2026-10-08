---
id: farmersdelight-b3878426
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/util/Mth.m_14159_(FFF)I
readable: net/minecraft/util/Mth.color(FFF)I
static: true
newOwner: net/minecraft/util/Mth
uses: 1
usedIn: vectorwing/farmersdelight/common/item/CookingPotItem
shimClass: rose.era.v1_20_1.shim.MthShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/MthShim.java
shimSignature: public static int color(float fValue, float fValue2, float fValue3)
ruleLine: net/minecraft/util/Mth.m_14159_(FFF)I	rose/era/v1_20_1/shim/MthShim.color	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/util/Mth.color(FFF)I`

The mod calls **net/minecraft/util/Mth.color(FFF)I** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `MthShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/MthShim.java`
- Signature: `public static int color(float fValue, float fValue2, float fValue3)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/util/Mth.m_14159_(FFF)I<TAB>rose/era/v1_20_1/shim/MthShim.color<TAB><evidence>`
