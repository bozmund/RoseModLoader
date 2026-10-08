---
id: farmersdelight-5438d94b
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/client/gui/screens/inventory/AbstractContainerScreen.m_6375_(DDI)Z
readable: net/minecraft/client/gui/screens/inventory/AbstractContainerScreen.mouseClicked(DDI)Z
static: false
newOwner: net/minecraft/client/gui/screens/inventory/AbstractContainerScreen
uses: 1
usedIn: vectorwing/farmersdelight/client/gui/CookingPotScreen
shimClass: rose.era.v1_20_1.shim.AbstractContainerScreenShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/AbstractContainerScreenShim.java
shimSignature: public static boolean mouseClicked(net.minecraft.client.gui.screens.inventory.AbstractContainerScreen self, double dValue, double dValue2, int iValue)
ruleLine: net/minecraft/client/gui/screens/inventory/AbstractContainerScreen.m_6375_(DDI)Z	rose/era/v1_20_1/shim/AbstractContainerScreenShim.mouseClicked	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/client/gui/screens/inventory/AbstractContainerScreen.mouseClicked(DDI)Z`

The mod calls **net/minecraft/client/gui/screens/inventory/AbstractContainerScreen.mouseClicked(DDI)Z** (1 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `AbstractContainerScreenShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/AbstractContainerScreenShim.java`
- Signature: `public static boolean mouseClicked(net.minecraft.client.gui.screens.inventory.AbstractContainerScreen self, double dValue, double dValue2, int iValue)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/client/gui/screens/inventory/AbstractContainerScreen.m_6375_(DDI)Z<TAB>rose/era/v1_20_1/shim/AbstractContainerScreenShim.mouseClicked<TAB><evidence>`
