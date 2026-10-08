---
id: farmersdelight-aa9bac46
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: com/mojang/blaze3d/vertex/PoseStack.m_252781_(Lorg/joml/Quaternionf;)V
readable: com/mojang/blaze3d/vertex/PoseStack.mulPose(Lorg/joml/Quaternionf;)V
static: false
newOwner: com/mojang/blaze3d/vertex/PoseStack
uses: 17
usedIn: vectorwing/farmersdelight/client/gui/CanvasSignEditScreen,vectorwing/farmersdelight/client/renderer/CanvasSignRenderer,vectorwing/farmersdelight/client/renderer/CuttingBoardRenderer,vectorwing/farmersdelight/client/renderer/DefaultStoveRenderer,vectorwing/farmersdelight/client/renderer/HangingCanvasSignRenderer,vectorwing/farmersdelight/client/renderer/SkilletItemRenderer,vectorwing/farmersdelight/client/renderer/SkilletRenderer
shimClass: rose.era.v1_20_1.shim.PoseStackShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/PoseStackShim.java
shimSignature: public static void mulPose(com.mojang.blaze3d.vertex.PoseStack self, org.joml.Quaternionf quaternionf)
ruleLine: com/mojang/blaze3d/vertex/PoseStack.m_252781_(Lorg/joml/Quaternionf;)V	rose/era/v1_20_1/shim/PoseStackShim.mulPose	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `com/mojang/blaze3d/vertex/PoseStack.mulPose(Lorg/joml/Quaternionf;)V`

The mod calls **com/mojang/blaze3d/vertex/PoseStack.mulPose(Lorg/joml/Quaternionf;)V** (17 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `PoseStackShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/PoseStackShim.java`
- Signature: `public static void mulPose(com.mojang.blaze3d.vertex.PoseStack self, org.joml.Quaternionf quaternionf)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `com/mojang/blaze3d/vertex/PoseStack.m_252781_(Lorg/joml/Quaternionf;)V<TAB>rose/era/v1_20_1/shim/PoseStackShim.mulPose<TAB><evidence>`
