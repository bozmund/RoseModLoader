---
id: farmersdelight-16de96e1
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/entity/player/Player.m_21166_(Lnet/minecraft/world/entity/EquipmentSlot;)V
readable: net/minecraft/world/entity/player/Player.broadcastBreakEvent(Lnet/minecraft/world/entity/EquipmentSlot;)V
static: false
newOwner: net/minecraft/world/entity/player/Player
uses: 1
usedIn: vectorwing/farmersdelight/common/block/entity/CuttingBoardBlockEntity
shimClass: rose.era.v1_20_1.shim.PlayerShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/PlayerShim.java
shimSignature: public static void broadcastBreakEvent(net.minecraft.world.entity.player.Player self, net.minecraft.world.entity.EquipmentSlot equipmentSlot)
ruleLine: net/minecraft/world/entity/player/Player.m_21166_(Lnet/minecraft/world/entity/EquipmentSlot;)V	rose/era/v1_20_1/shim/PlayerShim.broadcastBreakEvent	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/entity/player/Player.broadcastBreakEvent(Lnet/minecraft/world/entity/EquipmentSlot;)V`

The mod calls **net/minecraft/world/entity/player/Player.broadcastBreakEvent(Lnet/minecraft/world/entity/EquipmentSlot;)V** (1 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `PlayerShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/PlayerShim.java`
- Signature: `public static void broadcastBreakEvent(net.minecraft.world.entity.player.Player self, net.minecraft.world.entity.EquipmentSlot equipmentSlot)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/entity/player/Player.m_21166_(Lnet/minecraft/world/entity/EquipmentSlot;)V<TAB>rose/era/v1_20_1/shim/PlayerShim.broadcastBreakEvent<TAB><evidence>`
