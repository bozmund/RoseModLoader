---
id: farmersdelight-a8fdc2d8
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/client/Minecraft.m_91296_()F
readable: net/minecraft/client/Minecraft.getFrameTime()F
static: false
newOwner: net/minecraft/client/Minecraft
uses: 2
usedIn: vectorwing/farmersdelight/client/renderer/SkilletItemRenderer,vectorwing/farmersdelight/client/renderer/SkilletItemRenderer$ArmPoseTransformer
shimClass: rose.era.v1_20_1.shim.MinecraftShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/MinecraftShim.java
shimSignature: public static float getFrameTime(net.minecraft.client.Minecraft self)
ruleLine: net/minecraft/client/Minecraft.m_91296_()F	rose/era/v1_20_1/shim/MinecraftShim.getFrameTime	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/client/Minecraft.getFrameTime()F`

The mod calls **net/minecraft/client/Minecraft.getFrameTime()F** (2 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `MinecraftShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/MinecraftShim.java`
- Signature: `public static float getFrameTime(net.minecraft.client.Minecraft self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/client/Minecraft.m_91296_()F<TAB>rose/era/v1_20_1/shim/MinecraftShim.getFrameTime<TAB><evidence>`
