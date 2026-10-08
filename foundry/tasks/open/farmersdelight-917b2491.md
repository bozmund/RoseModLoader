---
id: farmersdelight-917b2491
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/entity/ai/attributes/AttributeModifier.m_22214_()Ljava/lang/String;
readable: net/minecraft/world/entity/ai/attributes/AttributeModifier.getName()Ljava/lang/String;
static: false
newOwner: net/minecraft/world/entity/ai/attributes/AttributeModifier
uses: 1
usedIn: vectorwing/farmersdelight/common/utility/TextUtils
shimClass: rose.era.v1_20_1.shim.AttributeModifierShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/AttributeModifierShim.java
shimSignature: public static java.lang.String getName(net.minecraft.world.entity.ai.attributes.AttributeModifier self)
ruleLine: net/minecraft/world/entity/ai/attributes/AttributeModifier.m_22214_()Ljava/lang/String;	rose/era/v1_20_1/shim/AttributeModifierShim.getName	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/entity/ai/attributes/AttributeModifier.getName()Ljava/lang/String;`

The mod calls **net/minecraft/world/entity/ai/attributes/AttributeModifier.getName()Ljava/lang/String;** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `AttributeModifierShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/AttributeModifierShim.java`
- Signature: `public static java.lang.String getName(net.minecraft.world.entity.ai.attributes.AttributeModifier self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/entity/ai/attributes/AttributeModifier.m_22214_()Ljava/lang/String;<TAB>rose/era/v1_20_1/shim/AttributeModifierShim.getName<TAB><evidence>`
