---
id: farmersdelight-74ad4420
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/entity/ai/attributes/AttributeModifier$Operation.m_22235_()I
readable: net/minecraft/world/entity/ai/attributes/AttributeModifier$Operation.toValue()I
static: false
newOwner: net/minecraft/world/entity/ai/attributes/AttributeModifier$Operation
uses: 2
usedIn: vectorwing/farmersdelight/common/utility/TextUtils
shimClass: rose.era.v1_20_1.shim.AttributeModifierOperationShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/AttributeModifierOperationShim.java
shimSignature: public static int toValue(net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation self)
ruleLine: net/minecraft/world/entity/ai/attributes/AttributeModifier$Operation.m_22235_()I	rose/era/v1_20_1/shim/AttributeModifierOperationShim.toValue	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/entity/ai/attributes/AttributeModifier$Operation.toValue()I`

The mod calls **net/minecraft/world/entity/ai/attributes/AttributeModifier$Operation.toValue()I** (2 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `AttributeModifierOperationShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/AttributeModifierOperationShim.java`
- Signature: `public static int toValue(net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/entity/ai/attributes/AttributeModifier$Operation.m_22235_()I<TAB>rose/era/v1_20_1/shim/AttributeModifierOperationShim.toValue<TAB><evidence>`
