---
id: farmersdelight-313f1c1c
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_MISSING
symbol: net/minecraft/world/entity/EntityType.m_204039_(Lnet/minecraft/tags/TagKey;)Z
readable: net/minecraft/world/entity/EntityType.is(Lnet/minecraft/tags/TagKey;)Z
static: false
newOwner: net/minecraft/world/entity/EntityType
uses: 3
usedIn: vectorwing/farmersdelight/common/event/CommonEvents,vectorwing/farmersdelight/common/item/DogFoodItem$DogFoodEvent,vectorwing/farmersdelight/common/item/HorseFeedItem$HorseFeedEvent
shimClass: rose.era.v1_20_1.shim.EntityTypeShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/EntityTypeShim.java
shimSignature: public static boolean is(net.minecraft.world.entity.EntityType self, net.minecraft.tags.TagKey tagKey)
ruleLine: net/minecraft/world/entity/EntityType.m_204039_(Lnet/minecraft/tags/TagKey;)Z	rose/era/v1_20_1/shim/EntityTypeShim.is	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/entity/EntityType.is(Lnet/minecraft/tags/TagKey;)Z`

The mod calls **net/minecraft/world/entity/EntityType.is(Lnet/minecraft/tags/TagKey;)Z** (3 uses), which can't be found in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `EntityTypeShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/EntityTypeShim.java`
- Signature: `public static boolean is(net.minecraft.world.entity.EntityType self, net.minecraft.tags.TagKey tagKey)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/entity/EntityType.m_204039_(Lnet/minecraft/tags/TagKey;)Z<TAB>rose/era/v1_20_1/shim/EntityTypeShim.is<TAB><evidence>`
