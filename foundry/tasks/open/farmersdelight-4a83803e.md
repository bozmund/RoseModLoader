---
id: farmersdelight-4a83803e
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_MISSING
symbol: net/minecraft/client/gui/Gui.m_93079_()I
readable: net/minecraft/client/gui/Gui.getGuiTicks()I
static: false
newOwner: net/minecraft/client/gui/Gui
uses: 2
usedIn: vectorwing/farmersdelight/client/gui/ComfortHealthOverlay,vectorwing/farmersdelight/client/gui/NourishmentHungerOverlay
shimClass: rose.era.v1_20_1.shim.GuiShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/GuiShim.java
shimSignature: public static int getGuiTicks(net.minecraft.client.gui.Gui self)
ruleLine: net/minecraft/client/gui/Gui.m_93079_()I	rose/era/v1_20_1/shim/GuiShim.getGuiTicks	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/client/gui/Gui.getGuiTicks()I`

The mod calls **net/minecraft/client/gui/Gui.getGuiTicks()I** (2 uses), which can't be found in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `GuiShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/GuiShim.java`
- Signature: `public static int getGuiTicks(net.minecraft.client.gui.Gui self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/client/gui/Gui.m_93079_()I<TAB>rose/era/v1_20_1/shim/GuiShim.getGuiTicks<TAB><evidence>`
