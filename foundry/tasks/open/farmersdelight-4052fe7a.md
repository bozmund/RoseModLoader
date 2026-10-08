---
id: farmersdelight-4052fe7a
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/entity/Entity.m_142535_(FFLnet/minecraft/world/damagesource/DamageSource;)Z
readable: net/minecraft/world/entity/Entity.causeFallDamage(FFLnet/minecraft/world/damagesource/DamageSource;)Z
static: false
newOwner: net/minecraft/world/entity/Entity
uses: 3
usedIn: vectorwing/farmersdelight/common/block/RiceBaleBlock,vectorwing/farmersdelight/common/block/RichSoilFarmlandBlock,vectorwing/farmersdelight/common/block/SafetyNetBlock
shimClass: rose.era.v1_20_1.shim.EntityShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/EntityShim.java
shimSignature: public static boolean causeFallDamage(net.minecraft.world.entity.Entity self, float fValue, float fValue2, net.minecraft.world.damagesource.DamageSource damageSource)
ruleLine: net/minecraft/world/entity/Entity.m_142535_(FFLnet/minecraft/world/damagesource/DamageSource;)Z	rose/era/v1_20_1/shim/EntityShim.causeFallDamage	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/entity/Entity.causeFallDamage(FFLnet/minecraft/world/damagesource/DamageSource;)Z`

The mod calls **net/minecraft/world/entity/Entity.causeFallDamage(FFLnet/minecraft/world/damagesource/DamageSource;)Z** (3 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `EntityShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/EntityShim.java`
- Signature: `public static boolean causeFallDamage(net.minecraft.world.entity.Entity self, float fValue, float fValue2, net.minecraft.world.damagesource.DamageSource damageSource)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/entity/Entity.m_142535_(FFLnet/minecraft/world/damagesource/DamageSource;)Z<TAB>rose/era/v1_20_1/shim/EntityShim.causeFallDamage<TAB><evidence>`
