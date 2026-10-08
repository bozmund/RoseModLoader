---
id: farmersdelight-fb872211
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/entity/LivingEntity.m_21195_(Lnet/minecraft/world/effect/MobEffect;)Z
readable: net/minecraft/world/entity/LivingEntity.removeEffect(Lnet/minecraft/world/effect/MobEffect;)Z
static: false
newOwner: net/minecraft/world/entity/LivingEntity
uses: 2
usedIn: vectorwing/farmersdelight/common/item/HotCocoaItem,vectorwing/farmersdelight/common/item/MilkBottleItem
shimClass: rose.era.v1_20_1.shim.LivingEntityShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/LivingEntityShim.java
shimSignature: public static boolean removeEffect(net.minecraft.world.entity.LivingEntity self, net.minecraft.world.effect.MobEffect mobEffect)
ruleLine: net/minecraft/world/entity/LivingEntity.m_21195_(Lnet/minecraft/world/effect/MobEffect;)Z	rose/era/v1_20_1/shim/LivingEntityShim.removeEffect	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/entity/LivingEntity.removeEffect(Lnet/minecraft/world/effect/MobEffect;)Z`

The mod calls **net/minecraft/world/entity/LivingEntity.removeEffect(Lnet/minecraft/world/effect/MobEffect;)Z** (2 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `LivingEntityShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/LivingEntityShim.java`
- Signature: `public static boolean removeEffect(net.minecraft.world.entity.LivingEntity self, net.minecraft.world.effect.MobEffect mobEffect)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/entity/LivingEntity.m_21195_(Lnet/minecraft/world/effect/MobEffect;)Z<TAB>rose/era/v1_20_1/shim/LivingEntityShim.removeEffect<TAB><evidence>`
