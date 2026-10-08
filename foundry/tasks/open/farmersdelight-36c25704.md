---
id: farmersdelight-36c25704
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/util/Mth.m_14031_(F)F
readable: net/minecraft/util/Mth.sin(F)F
static: true
newOwner: net/minecraft/util/Mth
uses: 5
usedIn: vectorwing/farmersdelight/client/renderer/SkilletItemRenderer,vectorwing/farmersdelight/client/renderer/SkilletItemRenderer$ArmPoseTransformer
shimClass: rose.era.v1_20_1.shim.MthShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/MthShim.java
shimSignature: public static float sin(float fValue)
ruleLine: net/minecraft/util/Mth.m_14031_(F)F	rose/era/v1_20_1/shim/MthShim.sin	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/util/Mth.sin(F)F`

The mod calls **net/minecraft/util/Mth.sin(F)F** (5 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `MthShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/MthShim.java`
- Signature: `public static float sin(float fValue)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/util/Mth.m_14031_(F)F<TAB>rose/era/v1_20_1/shim/MthShim.sin<TAB><evidence>`
