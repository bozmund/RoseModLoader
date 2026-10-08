---
id: farmersdelight-d2c5eaca
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/entity/LivingEntity.m_217043_()Lnet/minecraft/util/RandomSource;
readable: net/minecraft/world/entity/LivingEntity.getRandom()Lnet/minecraft/util/RandomSource;
static: false
newOwner: net/minecraft/world/entity/LivingEntity
uses: 1
usedIn: vectorwing/farmersdelight/common/item/SkilletItem$SkilletEvents
shimClass: rose.era.v1_20_1.shim.LivingEntityShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/LivingEntityShim.java
shimSignature: public static net.minecraft.util.RandomSource getRandom(net.minecraft.world.entity.LivingEntity self)
ruleLine: net/minecraft/world/entity/LivingEntity.m_217043_()Lnet/minecraft/util/RandomSource;	rose/era/v1_20_1/shim/LivingEntityShim.getRandom	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/entity/LivingEntity.getRandom()Lnet/minecraft/util/RandomSource;`

The mod calls **net/minecraft/world/entity/LivingEntity.getRandom()Lnet/minecraft/util/RandomSource;** (1 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `LivingEntityShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/LivingEntityShim.java`
- Signature: `public static net.minecraft.util.RandomSource getRandom(net.minecraft.world.entity.LivingEntity self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/entity/LivingEntity.m_217043_()Lnet/minecraft/util/RandomSource;<TAB>rose/era/v1_20_1/shim/LivingEntityShim.getRandom<TAB><evidence>`
