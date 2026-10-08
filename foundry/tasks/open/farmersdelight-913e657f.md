---
id: farmersdelight-913e657f
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/entity/LivingEntity.m_21124_(Lnet/minecraft/world/effect/MobEffect;)Lnet/minecraft/world/effect/MobEffectInstance;
readable: net/minecraft/world/entity/LivingEntity.getEffect(Lnet/minecraft/world/effect/MobEffect;)Lnet/minecraft/world/effect/MobEffectInstance;
static: false
newOwner: net/minecraft/world/entity/LivingEntity
uses: 2
usedIn: vectorwing/farmersdelight/common/item/HotCocoaItem,vectorwing/farmersdelight/common/item/MilkBottleItem
shimClass: rose.era.v1_20_1.shim.LivingEntityShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/LivingEntityShim.java
shimSignature: public static net.minecraft.world.effect.MobEffectInstance getEffect(net.minecraft.world.entity.LivingEntity self, net.minecraft.world.effect.MobEffect mobEffect)
ruleLine: net/minecraft/world/entity/LivingEntity.m_21124_(Lnet/minecraft/world/effect/MobEffect;)Lnet/minecraft/world/effect/MobEffectInstance;	rose/era/v1_20_1/shim/LivingEntityShim.getEffect	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/entity/LivingEntity.getEffect(Lnet/minecraft/world/effect/MobEffect;)Lnet/minecraft/world/effect/MobEffectInstance;`

The mod calls **net/minecraft/world/entity/LivingEntity.getEffect(Lnet/minecraft/world/effect/MobEffect;)Lnet/minecraft/world/effect/MobEffectInstance;** (2 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `LivingEntityShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/LivingEntityShim.java`
- Signature: `public static net.minecraft.world.effect.MobEffectInstance getEffect(net.minecraft.world.entity.LivingEntity self, net.minecraft.world.effect.MobEffect mobEffect)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/entity/LivingEntity.m_21124_(Lnet/minecraft/world/effect/MobEffect;)Lnet/minecraft/world/effect/MobEffectInstance;<TAB>rose/era/v1_20_1/shim/LivingEntityShim.getEffect<TAB><evidence>`
