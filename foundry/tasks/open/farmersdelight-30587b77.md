---
id: farmersdelight-30587b77
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/entity/npc/VillagerData.m_35571_()Lnet/minecraft/world/entity/npc/VillagerProfession;
readable: net/minecraft/world/entity/npc/VillagerData.getProfession()Lnet/minecraft/world/entity/npc/VillagerProfession;
static: false
newOwner: net/minecraft/world/entity/npc/villager/VillagerData
uses: 1
usedIn: vectorwing/farmersdelight/common/mixin/VillagersTargetRichSoilMixin
shimClass: rose.era.v1_20_1.shim.VillagerDataShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/VillagerDataShim.java
shimSignature: public static net.minecraft.world.entity.npc.villager.VillagerProfession getProfession(net.minecraft.world.entity.npc.villager.VillagerData self)
ruleLine: net/minecraft/world/entity/npc/VillagerData.m_35571_()Lnet/minecraft/world/entity/npc/VillagerProfession;	rose/era/v1_20_1/shim/VillagerDataShim.getProfession	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/entity/npc/VillagerData.getProfession()Lnet/minecraft/world/entity/npc/VillagerProfession;`

The mod calls **net/minecraft/world/entity/npc/VillagerData.getProfession()Lnet/minecraft/world/entity/npc/VillagerProfession;** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `VillagerDataShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/VillagerDataShim.java`
- Signature: `public static net.minecraft.world.entity.npc.villager.VillagerProfession getProfession(net.minecraft.world.entity.npc.villager.VillagerData self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/entity/npc/VillagerData.m_35571_()Lnet/minecraft/world/entity/npc/VillagerProfession;<TAB>rose/era/v1_20_1/shim/VillagerDataShim.getProfession<TAB><evidence>`
