---
id: farmersdelight-91e87c94
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/entity/LivingEntity.m_21166_(Lnet/minecraft/world/entity/EquipmentSlot;)V
readable: net/minecraft/world/entity/LivingEntity.broadcastBreakEvent(Lnet/minecraft/world/entity/EquipmentSlot;)V
static: false
newOwner: net/minecraft/world/entity/LivingEntity
uses: 3
usedIn: vectorwing/farmersdelight/common/item/KnifeItem,vectorwing/farmersdelight/common/item/SkilletItem
shimClass: rose.era.v1_20_1.shim.LivingEntityShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/LivingEntityShim.java
shimSignature: public static void broadcastBreakEvent(net.minecraft.world.entity.LivingEntity self, net.minecraft.world.entity.EquipmentSlot equipmentSlot)
ruleLine: net/minecraft/world/entity/LivingEntity.m_21166_(Lnet/minecraft/world/entity/EquipmentSlot;)V	rose/era/v1_20_1/shim/LivingEntityShim.broadcastBreakEvent	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/entity/LivingEntity.broadcastBreakEvent(Lnet/minecraft/world/entity/EquipmentSlot;)V`

The mod calls **net/minecraft/world/entity/LivingEntity.broadcastBreakEvent(Lnet/minecraft/world/entity/EquipmentSlot;)V** (3 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `LivingEntityShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/LivingEntityShim.java`
- Signature: `public static void broadcastBreakEvent(net.minecraft.world.entity.LivingEntity self, net.minecraft.world.entity.EquipmentSlot equipmentSlot)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/entity/LivingEntity.m_21166_(Lnet/minecraft/world/entity/EquipmentSlot;)V<TAB>rose/era/v1_20_1/shim/LivingEntityShim.broadcastBreakEvent<TAB><evidence>`
