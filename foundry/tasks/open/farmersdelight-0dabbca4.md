---
id: farmersdelight-0dabbca4
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_MISSING
symbol: net/minecraft/world/level/block/entity/SignBlockEntity.m_277142_()Lnet/minecraft/world/level/block/entity/SignText;
readable: net/minecraft/world/level/block/entity/SignBlockEntity.getFrontText()Lnet/minecraft/world/level/block/entity/SignText;
static: false
newOwner: net/minecraft/world/level/block/entity/SignBlockEntity
uses: 1
usedIn: vectorwing/farmersdelight/client/renderer/CanvasSignRenderer
shimClass: rose.era.v1_20_1.shim.SignBlockEntityShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/SignBlockEntityShim.java
shimSignature: public static net.minecraft.world.level.block.entity.SignText getFrontText(net.minecraft.world.level.block.entity.SignBlockEntity self)
ruleLine: net/minecraft/world/level/block/entity/SignBlockEntity.m_277142_()Lnet/minecraft/world/level/block/entity/SignText;	rose/era/v1_20_1/shim/SignBlockEntityShim.getFrontText	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/level/block/entity/SignBlockEntity.getFrontText()Lnet/minecraft/world/level/block/entity/SignText;`

The mod calls **net/minecraft/world/level/block/entity/SignBlockEntity.getFrontText()Lnet/minecraft/world/level/block/entity/SignText;** (1 uses), which can't be found in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `SignBlockEntityShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/SignBlockEntityShim.java`
- Signature: `public static net.minecraft.world.level.block.entity.SignText getFrontText(net.minecraft.world.level.block.entity.SignBlockEntity self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/level/block/entity/SignBlockEntity.m_277142_()Lnet/minecraft/world/level/block/entity/SignText;<TAB>rose/era/v1_20_1/shim/SignBlockEntityShim.getFrontText<TAB><evidence>`
