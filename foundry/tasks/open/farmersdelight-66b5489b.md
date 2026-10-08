---
id: farmersdelight-66b5489b
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/client/multiplayer/ClientLevel.m_46467_()J
readable: net/minecraft/client/multiplayer/ClientLevel.getGameTime()J
static: false
newOwner: net/minecraft/client/multiplayer/ClientLevel
uses: 1
usedIn: vectorwing/farmersdelight/client/renderer/SkilletItemRenderer
shimClass: rose.era.v1_20_1.shim.ClientLevelShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ClientLevelShim.java
shimSignature: public static long getGameTime(net.minecraft.client.multiplayer.ClientLevel self)
ruleLine: net/minecraft/client/multiplayer/ClientLevel.m_46467_()J	rose/era/v1_20_1/shim/ClientLevelShim.getGameTime	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/client/multiplayer/ClientLevel.getGameTime()J`

The mod calls **net/minecraft/client/multiplayer/ClientLevel.getGameTime()J** (1 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `ClientLevelShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ClientLevelShim.java`
- Signature: `public static long getGameTime(net.minecraft.client.multiplayer.ClientLevel self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/client/multiplayer/ClientLevel.m_46467_()J<TAB>rose/era/v1_20_1/shim/ClientLevelShim.getGameTime<TAB><evidence>`
