---
id: farmersdelight-f85aa310
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/advancements/CriteriaTriggers.m_10595_(Lnet/minecraft/advancements/CriterionTrigger;)Lnet/minecraft/advancements/CriterionTrigger;
readable: net/minecraft/advancements/CriteriaTriggers.register(Lnet/minecraft/advancements/CriterionTrigger;)Lnet/minecraft/advancements/CriterionTrigger;
static: true
newOwner: net/minecraft/advancements/triggers/CriteriaTriggers
uses: 1
usedIn: vectorwing/farmersdelight/common/registry/ModAdvancements
shimClass: rose.era.v1_20_1.shim.CriteriaTriggersShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/CriteriaTriggersShim.java
shimSignature: public static net.minecraft.advancements.triggers.CriterionTrigger register(net.minecraft.advancements.triggers.CriterionTrigger criterionTrigger)
ruleLine: net/minecraft/advancements/CriteriaTriggers.m_10595_(Lnet/minecraft/advancements/CriterionTrigger;)Lnet/minecraft/advancements/CriterionTrigger;	rose/era/v1_20_1/shim/CriteriaTriggersShim.register	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/advancements/CriteriaTriggers.register(Lnet/minecraft/advancements/CriterionTrigger;)Lnet/minecraft/advancements/CriterionTrigger;`

The mod calls **net/minecraft/advancements/CriteriaTriggers.register(Lnet/minecraft/advancements/CriterionTrigger;)Lnet/minecraft/advancements/CriterionTrigger;** (1 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `CriteriaTriggersShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/CriteriaTriggersShim.java`
- Signature: `public static net.minecraft.advancements.triggers.CriterionTrigger register(net.minecraft.advancements.triggers.CriterionTrigger criterionTrigger)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/advancements/CriteriaTriggers.m_10595_(Lnet/minecraft/advancements/CriterionTrigger;)Lnet/minecraft/advancements/CriterionTrigger;<TAB>rose/era/v1_20_1/shim/CriteriaTriggersShim.register<TAB><evidence>`
