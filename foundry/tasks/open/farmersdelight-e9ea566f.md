---
id: farmersdelight-e9ea566f
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/item/BlockItem.m_7167_(Lnet/minecraft/world/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;
readable: net/minecraft/world/item/BlockItem.getDefaultAttributeModifiers(Lnet/minecraft/world/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;
static: false
newOwner: net/minecraft/world/item/BlockItem
uses: 1
usedIn: vectorwing/farmersdelight/common/item/SkilletItem
shimClass: rose.era.v1_20_1.shim.BlockItemShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockItemShim.java
shimSignature: public static com.google.common.collect.Multimap getDefaultAttributeModifiers(net.minecraft.world.item.BlockItem self, net.minecraft.world.entity.EquipmentSlot equipmentSlot)
ruleLine: net/minecraft/world/item/BlockItem.m_7167_(Lnet/minecraft/world/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;	rose/era/v1_20_1/shim/BlockItemShim.getDefaultAttributeModifiers	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/BlockItem.getDefaultAttributeModifiers(Lnet/minecraft/world/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;`

The mod calls **net/minecraft/world/item/BlockItem.getDefaultAttributeModifiers(Lnet/minecraft/world/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `BlockItemShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/BlockItemShim.java`
- Signature: `public static com.google.common.collect.Multimap getDefaultAttributeModifiers(net.minecraft.world.item.BlockItem self, net.minecraft.world.entity.EquipmentSlot equipmentSlot)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/BlockItem.m_7167_(Lnet/minecraft/world/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;<TAB>rose/era/v1_20_1/shim/BlockItemShim.getDefaultAttributeModifiers<TAB><evidence>`
