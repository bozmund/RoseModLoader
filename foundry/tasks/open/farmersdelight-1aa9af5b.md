---
id: farmersdelight-1aa9af5b
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/entity/monster/piglin/PiglinAi.m_34873_(Lnet/minecraft/world/entity/player/Player;Z)V
readable: net/minecraft/world/entity/monster/piglin/PiglinAi.angerNearbyPiglins(Lnet/minecraft/world/entity/player/Player;Z)V
static: true
newOwner: net/minecraft/world/entity/monster/piglin/PiglinAi
uses: 1
usedIn: vectorwing/farmersdelight/common/block/CabinetBlock
shimClass: rose.era.v1_20_1.shim.PiglinAiShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/PiglinAiShim.java
shimSignature: public static void angerNearbyPiglins(net.minecraft.world.entity.player.Player player, boolean bValue)
ruleLine: net/minecraft/world/entity/monster/piglin/PiglinAi.m_34873_(Lnet/minecraft/world/entity/player/Player;Z)V	rose/era/v1_20_1/shim/PiglinAiShim.angerNearbyPiglins	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/entity/monster/piglin/PiglinAi.angerNearbyPiglins(Lnet/minecraft/world/entity/player/Player;Z)V`

The mod calls **net/minecraft/world/entity/monster/piglin/PiglinAi.angerNearbyPiglins(Lnet/minecraft/world/entity/player/Player;Z)V** (1 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `PiglinAiShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/PiglinAiShim.java`
- Signature: `public static void angerNearbyPiglins(net.minecraft.world.entity.player.Player player, boolean bValue)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/entity/monster/piglin/PiglinAi.m_34873_(Lnet/minecraft/world/entity/player/Player;Z)V<TAB>rose/era/v1_20_1/shim/PiglinAiShim.angerNearbyPiglins<TAB><evidence>`
