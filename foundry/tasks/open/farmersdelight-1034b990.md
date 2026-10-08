---
id: farmersdelight-1034b990
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/effect/MobEffect.m_19485_()Ljava/util/Map;
readable: net/minecraft/world/effect/MobEffect.getAttributeModifiers()Ljava/util/Map;
static: false
newOwner: net/minecraft/world/effect/MobEffect
uses: 1
usedIn: vectorwing/farmersdelight/common/utility/TextUtils
shimClass: rose.era.v1_20_1.shim.MobEffectShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/MobEffectShim.java
shimSignature: public static java.util.Map getAttributeModifiers(net.minecraft.world.effect.MobEffect self)
ruleLine: net/minecraft/world/effect/MobEffect.m_19485_()Ljava/util/Map;	rose/era/v1_20_1/shim/MobEffectShim.getAttributeModifiers	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/effect/MobEffect.getAttributeModifiers()Ljava/util/Map;`

The mod calls **net/minecraft/world/effect/MobEffect.getAttributeModifiers()Ljava/util/Map;** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `MobEffectShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/MobEffectShim.java`
- Signature: `public static java.util.Map getAttributeModifiers(net.minecraft.world.effect.MobEffect self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/effect/MobEffect.m_19485_()Ljava/util/Map;<TAB>rose/era/v1_20_1/shim/MobEffectShim.getAttributeModifiers<TAB><evidence>`
