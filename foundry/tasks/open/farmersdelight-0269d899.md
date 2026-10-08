---
id: farmersdelight-0269d899
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/entity/ai/attributes/AttributeModifier.m_22217_()Lnet/minecraft/world/entity/ai/attributes/AttributeModifier$Operation;
readable: net/minecraft/world/entity/ai/attributes/AttributeModifier.getOperation()Lnet/minecraft/world/entity/ai/attributes/AttributeModifier$Operation;
static: false
newOwner: net/minecraft/world/entity/ai/attributes/AttributeModifier
uses: 5
usedIn: vectorwing/farmersdelight/common/utility/TextUtils
shimClass: rose.era.v1_20_1.shim.AttributeModifierShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/AttributeModifierShim.java
shimSignature: public static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation getOperation(net.minecraft.world.entity.ai.attributes.AttributeModifier self)
ruleLine: net/minecraft/world/entity/ai/attributes/AttributeModifier.m_22217_()Lnet/minecraft/world/entity/ai/attributes/AttributeModifier$Operation;	rose/era/v1_20_1/shim/AttributeModifierShim.getOperation	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/entity/ai/attributes/AttributeModifier.getOperation()Lnet/minecraft/world/entity/ai/attributes/AttributeModifier$Operation;`

The mod calls **net/minecraft/world/entity/ai/attributes/AttributeModifier.getOperation()Lnet/minecraft/world/entity/ai/attributes/AttributeModifier$Operation;** (5 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `AttributeModifierShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/AttributeModifierShim.java`
- Signature: `public static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation getOperation(net.minecraft.world.entity.ai.attributes.AttributeModifier self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/entity/ai/attributes/AttributeModifier.m_22217_()Lnet/minecraft/world/entity/ai/attributes/AttributeModifier$Operation;<TAB>rose/era/v1_20_1/shim/AttributeModifierShim.getOperation<TAB><evidence>`
