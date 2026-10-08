---
id: farmersdelight-05533f4e
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/client/renderer/blockentity/BlockEntityRendererProvider$Context.m_173586_()Lnet/minecraft/client/gui/Font;
readable: net/minecraft/client/renderer/blockentity/BlockEntityRendererProvider$Context.getFont()Lnet/minecraft/client/gui/Font;
static: false
newOwner: net/minecraft/client/renderer/blockentity/BlockEntityRendererProvider$Context
uses: 1
usedIn: vectorwing/farmersdelight/client/renderer/CanvasSignRenderer
shimClass: rose.era.v1_20_1.shim.BlockEntityRendererProviderContextShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockEntityRendererProviderContextShim.java
shimSignature: public static net.minecraft.client.gui.Font getFont(net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context self)
ruleLine: net/minecraft/client/renderer/blockentity/BlockEntityRendererProvider$Context.m_173586_()Lnet/minecraft/client/gui/Font;	rose/era/v1_20_1/shim/BlockEntityRendererProviderContextShim.getFont	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/client/renderer/blockentity/BlockEntityRendererProvider$Context.getFont()Lnet/minecraft/client/gui/Font;`

The mod calls **net/minecraft/client/renderer/blockentity/BlockEntityRendererProvider$Context.getFont()Lnet/minecraft/client/gui/Font;** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `BlockEntityRendererProviderContextShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockEntityRendererProviderContextShim.java`
- Signature: `public static net.minecraft.client.gui.Font getFont(net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/client/renderer/blockentity/BlockEntityRendererProvider$Context.m_173586_()Lnet/minecraft/client/gui/Font;<TAB>rose/era/v1_20_1/shim/BlockEntityRendererProviderContextShim.getFont<TAB><evidence>`
