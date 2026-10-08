---
id: farmersdelight-e2e50ae8
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_MISSING
symbol: net/minecraft/ChatFormatting.m_126665_()Ljava/lang/Integer;
readable: net/minecraft/ChatFormatting.getColor()Ljava/lang/Integer;
static: false
newOwner: net/minecraft/ChatFormatting
uses: 1
usedIn: vectorwing/farmersdelight/client/gui/CookingPotTooltip
shimClass: rose.era.v1_20_1.shim.ChatFormattingShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ChatFormattingShim.java
shimSignature: public static java.lang.Integer getColor(net.minecraft.ChatFormatting self)
ruleLine: net/minecraft/ChatFormatting.m_126665_()Ljava/lang/Integer;	rose/era/v1_20_1/shim/ChatFormattingShim.getColor	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/ChatFormatting.getColor()Ljava/lang/Integer;`

The mod calls **net/minecraft/ChatFormatting.getColor()Ljava/lang/Integer;** (1 uses), which can't be found in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `ChatFormattingShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ChatFormattingShim.java`
- Signature: `public static java.lang.Integer getColor(net.minecraft.ChatFormatting self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/ChatFormatting.m_126665_()Ljava/lang/Integer;<TAB>rose/era/v1_20_1/shim/ChatFormattingShim.getColor<TAB><evidence>`
