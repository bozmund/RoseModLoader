---
id: farmersdelight-b39ead78
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/entity/player/Player.m_21190_(Lnet/minecraft/world/InteractionHand;)V
readable: net/minecraft/world/entity/player/Player.broadcastBreakEvent(Lnet/minecraft/world/InteractionHand;)V
static: false
newOwner: net/minecraft/world/entity/player/Player
uses: 4
usedIn: vectorwing/farmersdelight/common/block/AbstractStoveBlock,vectorwing/farmersdelight/common/block/MushroomColonyBlock
shimClass: rose.era.v1_20_1.shim.PlayerShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/PlayerShim.java
shimSignature: public static void broadcastBreakEvent(net.minecraft.world.entity.player.Player self, net.minecraft.world.InteractionHand interactionHand)
ruleLine: net/minecraft/world/entity/player/Player.m_21190_(Lnet/minecraft/world/InteractionHand;)V	rose/era/v1_20_1/shim/PlayerShim.broadcastBreakEvent	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/entity/player/Player.broadcastBreakEvent(Lnet/minecraft/world/InteractionHand;)V`

The mod calls **net/minecraft/world/entity/player/Player.broadcastBreakEvent(Lnet/minecraft/world/InteractionHand;)V** (4 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `PlayerShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/PlayerShim.java`
- Signature: `public static void broadcastBreakEvent(net.minecraft.world.entity.player.Player self, net.minecraft.world.InteractionHand interactionHand)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/entity/player/Player.m_21190_(Lnet/minecraft/world/InteractionHand;)V<TAB>rose/era/v1_20_1/shim/PlayerShim.broadcastBreakEvent<TAB><evidence>`
