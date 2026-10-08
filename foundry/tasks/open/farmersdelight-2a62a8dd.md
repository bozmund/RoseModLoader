---
id: farmersdelight-2a62a8dd
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/entity/EntityType$Builder.m_20712_(Ljava/lang/String;)Lnet/minecraft/world/entity/EntityType;
readable: net/minecraft/world/entity/EntityType$Builder.build(Ljava/lang/String;)Lnet/minecraft/world/entity/EntityType;
static: false
newOwner: net/minecraft/world/entity/EntityType$Builder
uses: 1
usedIn: vectorwing/farmersdelight/common/registry/ModEntityTypes
shimClass: rose.era.v1_20_1.shim.EntityTypeBuilderShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/EntityTypeBuilderShim.java
shimSignature: public static net.minecraft.world.entity.EntityType build(net.minecraft.world.entity.EntityType.Builder self, java.lang.String string)
ruleLine: net/minecraft/world/entity/EntityType$Builder.m_20712_(Ljava/lang/String;)Lnet/minecraft/world/entity/EntityType;	rose/era/v1_20_1/shim/EntityTypeBuilderShim.build	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/entity/EntityType$Builder.build(Ljava/lang/String;)Lnet/minecraft/world/entity/EntityType;`

The mod calls **net/minecraft/world/entity/EntityType$Builder.build(Ljava/lang/String;)Lnet/minecraft/world/entity/EntityType;** (1 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `EntityTypeBuilderShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/EntityTypeBuilderShim.java`
- Signature: `public static net.minecraft.world.entity.EntityType build(net.minecraft.world.entity.EntityType.Builder self, java.lang.String string)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/entity/EntityType$Builder.m_20712_(Ljava/lang/String;)Lnet/minecraft/world/entity/EntityType;<TAB>rose/era/v1_20_1/shim/EntityTypeBuilderShim.build<TAB><evidence>`
