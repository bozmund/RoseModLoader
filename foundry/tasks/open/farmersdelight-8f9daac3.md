---
id: farmersdelight-8f9daac3
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/effect/MobEffectInstance.m_19544_()Lnet/minecraft/world/effect/MobEffect;
readable: net/minecraft/world/effect/MobEffectInstance.getEffect()Lnet/minecraft/world/effect/MobEffect;
static: false
newOwner: net/minecraft/world/effect/MobEffectInstance
uses: 9
usedIn: vectorwing/farmersdelight/client/event/TooltipEvents,vectorwing/farmersdelight/common/item/DogFoodItem,vectorwing/farmersdelight/common/item/HorseFeedItem,vectorwing/farmersdelight/common/item/HotCocoaItem,vectorwing/farmersdelight/common/item/MilkBottleItem,vectorwing/farmersdelight/common/utility/TextUtils
shimClass: rose.era.v1_20_1.shim.MobEffectInstanceShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/MobEffectInstanceShim.java
shimSignature: public static net.minecraft.world.effect.MobEffect getEffect(net.minecraft.world.effect.MobEffectInstance self)
ruleLine: net/minecraft/world/effect/MobEffectInstance.m_19544_()Lnet/minecraft/world/effect/MobEffect;	rose/era/v1_20_1/shim/MobEffectInstanceShim.getEffect	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/effect/MobEffectInstance.getEffect()Lnet/minecraft/world/effect/MobEffect;`

The mod calls **net/minecraft/world/effect/MobEffectInstance.getEffect()Lnet/minecraft/world/effect/MobEffect;** (9 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `MobEffectInstanceShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/MobEffectInstanceShim.java`
- Signature: `public static net.minecraft.world.effect.MobEffect getEffect(net.minecraft.world.effect.MobEffectInstance self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/effect/MobEffectInstance.m_19544_()Lnet/minecraft/world/effect/MobEffect;<TAB>rose/era/v1_20_1/shim/MobEffectInstanceShim.getEffect<TAB><evidence>`
