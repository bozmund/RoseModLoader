---
id: farmersdelight-72af2a69
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_MISSING
symbol: net/minecraft/world/level/block/entity/SignText.m_276901_(Lnet/minecraft/world/item/DyeColor;)Lnet/minecraft/world/level/block/entity/SignText;
readable: net/minecraft/world/level/block/entity/SignText.setColor(Lnet/minecraft/world/item/DyeColor;)Lnet/minecraft/world/level/block/entity/SignText;
static: false
newOwner: net/minecraft/world/level/block/entity/SignText
uses: 3
usedIn: vectorwing/farmersdelight/common/block/entity/CanvasSignBlockEntity,vectorwing/farmersdelight/common/block/entity/HangingCanvasSignBlockEntity
shimClass: rose.era.v1_20_1.shim.SignTextShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/SignTextShim.java
shimSignature: public static net.minecraft.world.level.block.entity.SignText setColor(net.minecraft.world.level.block.entity.SignText self, net.minecraft.world.item.DyeColor dyeColor)
ruleLine: net/minecraft/world/level/block/entity/SignText.m_276901_(Lnet/minecraft/world/item/DyeColor;)Lnet/minecraft/world/level/block/entity/SignText;	rose/era/v1_20_1/shim/SignTextShim.setColor	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/level/block/entity/SignText.setColor(Lnet/minecraft/world/item/DyeColor;)Lnet/minecraft/world/level/block/entity/SignText;`

The mod calls **net/minecraft/world/level/block/entity/SignText.setColor(Lnet/minecraft/world/item/DyeColor;)Lnet/minecraft/world/level/block/entity/SignText;** (3 uses), which can't be found in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `SignTextShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/SignTextShim.java`
- Signature: `public static net.minecraft.world.level.block.entity.SignText setColor(net.minecraft.world.level.block.entity.SignText self, net.minecraft.world.item.DyeColor dyeColor)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/level/block/entity/SignText.m_276901_(Lnet/minecraft/world/item/DyeColor;)Lnet/minecraft/world/level/block/entity/SignText;<TAB>rose/era/v1_20_1/shim/SignTextShim.setColor<TAB><evidence>`
