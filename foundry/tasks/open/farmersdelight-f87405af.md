---
id: farmersdelight-f87405af
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_MISSING
symbol: net/minecraft/client/Minecraft.m_91152_(Lnet/minecraft/client/gui/screens/Screen;)V
readable: net/minecraft/client/Minecraft.setScreen(Lnet/minecraft/client/gui/screens/Screen;)V
static: false
newOwner: net/minecraft/client/Minecraft
uses: 2
usedIn: vectorwing/farmersdelight/common/mixin/CanvasSignEditScreenMixin
shimClass: rose.era.v1_20_1.shim.MinecraftShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/MinecraftShim.java
shimSignature: public static void setScreen(net.minecraft.client.Minecraft self, net.minecraft.client.gui.screens.Screen screen)
ruleLine: net/minecraft/client/Minecraft.m_91152_(Lnet/minecraft/client/gui/screens/Screen;)V	rose/era/v1_20_1/shim/MinecraftShim.setScreen	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/client/Minecraft.setScreen(Lnet/minecraft/client/gui/screens/Screen;)V`

The mod calls **net/minecraft/client/Minecraft.setScreen(Lnet/minecraft/client/gui/screens/Screen;)V** (2 uses), which can't be found in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `MinecraftShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/MinecraftShim.java`
- Signature: `public static void setScreen(net.minecraft.client.Minecraft self, net.minecraft.client.gui.screens.Screen screen)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/client/Minecraft.m_91152_(Lnet/minecraft/client/gui/screens/Screen;)V<TAB>rose/era/v1_20_1/shim/MinecraftShim.setScreen<TAB><evidence>`
