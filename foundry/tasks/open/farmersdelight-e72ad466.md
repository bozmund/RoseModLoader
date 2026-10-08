---
id: farmersdelight-e72ad466
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/entity/Entity.m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z
readable: net/minecraft/world/entity/Entity.hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z
static: false
newOwner: net/minecraft/world/entity/Entity
uses: 2
usedIn: vectorwing/farmersdelight/common/block/AbstractStoveBlock,vectorwing/farmersdelight/common/entity/RottenTomatoEntity
shimClass: rose.era.v1_20_1.shim.EntityShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/EntityShim.java
shimSignature: public static boolean hurt(net.minecraft.world.entity.Entity self, net.minecraft.world.damagesource.DamageSource damageSource, float fValue)
ruleLine: net/minecraft/world/entity/Entity.m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z	rose/era/v1_20_1/shim/EntityShim.hurt	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/entity/Entity.hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z`

The mod calls **net/minecraft/world/entity/Entity.hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z** (2 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `EntityShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/EntityShim.java`
- Signature: `public static boolean hurt(net.minecraft.world.entity.Entity self, net.minecraft.world.damagesource.DamageSource damageSource, float fValue)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/entity/Entity.m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z<TAB>rose/era/v1_20_1/shim/EntityShim.hurt<TAB><evidence>`
