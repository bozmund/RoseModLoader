---
id: farmersdelight-9981d5ba
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/effect/MobEffectUtil.m_267641_(Lnet/minecraft/world/effect/MobEffectInstance;F)Lnet/minecraft/network/chat/Component;
readable: net/minecraft/world/effect/MobEffectUtil.formatDuration(Lnet/minecraft/world/effect/MobEffectInstance;F)Lnet/minecraft/network/chat/Component;
static: true
newOwner: net/minecraft/world/effect/MobEffectUtil
uses: 4
usedIn: vectorwing/farmersdelight/client/event/TooltipEvents,vectorwing/farmersdelight/common/item/DogFoodItem,vectorwing/farmersdelight/common/item/HorseFeedItem,vectorwing/farmersdelight/common/utility/TextUtils
shimClass: rose.era.v1_20_1.shim.MobEffectUtilShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/MobEffectUtilShim.java
shimSignature: public static net.minecraft.network.chat.Component formatDuration(net.minecraft.world.effect.MobEffectInstance mobEffectInstance, float fValue)
ruleLine: net/minecraft/world/effect/MobEffectUtil.m_267641_(Lnet/minecraft/world/effect/MobEffectInstance;F)Lnet/minecraft/network/chat/Component;	rose/era/v1_20_1/shim/MobEffectUtilShim.formatDuration	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/effect/MobEffectUtil.formatDuration(Lnet/minecraft/world/effect/MobEffectInstance;F)Lnet/minecraft/network/chat/Component;`

The mod calls **net/minecraft/world/effect/MobEffectUtil.formatDuration(Lnet/minecraft/world/effect/MobEffectInstance;F)Lnet/minecraft/network/chat/Component;** (4 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `MobEffectUtilShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/MobEffectUtilShim.java`
- Signature: `public static net.minecraft.network.chat.Component formatDuration(net.minecraft.world.effect.MobEffectInstance mobEffectInstance, float fValue)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/effect/MobEffectUtil.m_267641_(Lnet/minecraft/world/effect/MobEffectInstance;F)Lnet/minecraft/network/chat/Component;<TAB>rose/era/v1_20_1/shim/MobEffectUtilShim.formatDuration<TAB><evidence>`
