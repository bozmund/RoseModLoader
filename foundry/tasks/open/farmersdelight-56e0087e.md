---
id: farmersdelight-56e0087e
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/core/Direction.m_122436_()Lnet/minecraft/core/Vec3i;
readable: net/minecraft/core/Direction.getNormal()Lnet/minecraft/core/Vec3i;
static: false
newOwner: net/minecraft/core/Direction
uses: 1
usedIn: vectorwing/farmersdelight/common/block/entity/CabinetBlockEntity
shimClass: rose.era.v1_20_1.shim.DirectionShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/DirectionShim.java
shimSignature: public static net.minecraft.core.Vec3i getNormal(net.minecraft.core.Direction self)
ruleLine: net/minecraft/core/Direction.m_122436_()Lnet/minecraft/core/Vec3i;	rose/era/v1_20_1/shim/DirectionShim.getNormal	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/core/Direction.getNormal()Lnet/minecraft/core/Vec3i;`

The mod calls **net/minecraft/core/Direction.getNormal()Lnet/minecraft/core/Vec3i;** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `DirectionShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/DirectionShim.java`
- Signature: `public static net.minecraft.core.Vec3i getNormal(net.minecraft.core.Direction self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/core/Direction.m_122436_()Lnet/minecraft/core/Vec3i;<TAB>rose/era/v1_20_1/shim/DirectionShim.getNormal<TAB><evidence>`
