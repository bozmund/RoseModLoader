---
id: farmersdelight-ca45198e
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/effect/MobEffect.m_7048_(ILnet/minecraft/world/entity/ai/attributes/AttributeModifier;)D
readable: net/minecraft/world/effect/MobEffect.getAttributeModifierValue(ILnet/minecraft/world/entity/ai/attributes/AttributeModifier;)D
static: false
newOwner: net/minecraft/world/effect/MobEffect
uses: 1
usedIn: vectorwing/farmersdelight/common/utility/TextUtils
shimClass: rose.era.v1_20_1.shim.MobEffectShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/MobEffectShim.java
shimSignature: public static double getAttributeModifierValue(net.minecraft.world.effect.MobEffect self, int iValue, net.minecraft.world.entity.ai.attributes.AttributeModifier attributeModifier)
ruleLine: net/minecraft/world/effect/MobEffect.m_7048_(ILnet/minecraft/world/entity/ai/attributes/AttributeModifier;)D	rose/era/v1_20_1/shim/MobEffectShim.getAttributeModifierValue	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/effect/MobEffect.getAttributeModifierValue(ILnet/minecraft/world/entity/ai/attributes/AttributeModifier;)D`

The mod calls **net/minecraft/world/effect/MobEffect.getAttributeModifierValue(ILnet/minecraft/world/entity/ai/attributes/AttributeModifier;)D** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `MobEffectShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/MobEffectShim.java`
- Signature: `public static double getAttributeModifierValue(net.minecraft.world.effect.MobEffect self, int iValue, net.minecraft.world.entity.ai.attributes.AttributeModifier attributeModifier)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/effect/MobEffect.m_7048_(ILnet/minecraft/world/entity/ai/attributes/AttributeModifier;)D<TAB>rose/era/v1_20_1/shim/MobEffectShim.getAttributeModifierValue<TAB><evidence>`
