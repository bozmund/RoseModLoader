---
id: farmersdelight-6f6731b8
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/item/ItemStack.m_41622_(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V
readable: net/minecraft/world/item/ItemStack.hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V
static: false
newOwner: net/minecraft/world/item/ItemStack
uses: 8
usedIn: vectorwing/farmersdelight/common/block/AbstractStoveBlock,vectorwing/farmersdelight/common/block/MushroomColonyBlock,vectorwing/farmersdelight/common/block/entity/CuttingBoardBlockEntity,vectorwing/farmersdelight/common/item/KnifeItem,vectorwing/farmersdelight/common/item/SkilletItem
shimClass: rose.era.v1_20_1.shim.ItemStackShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemStackShim.java
shimSignature: public static void hurtAndBreak(net.minecraft.world.item.ItemStack self, int iValue, net.minecraft.world.entity.LivingEntity livingEntity, java.util.function.Consumer consumer)
ruleLine: net/minecraft/world/item/ItemStack.m_41622_(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V	rose/era/v1_20_1/shim/ItemStackShim.hurtAndBreak	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/ItemStack.hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V`

The mod calls **net/minecraft/world/item/ItemStack.hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V** (8 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `ItemStackShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemStackShim.java`
- Signature: `public static void hurtAndBreak(net.minecraft.world.item.ItemStack self, int iValue, net.minecraft.world.entity.LivingEntity livingEntity, java.util.function.Consumer consumer)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/ItemStack.m_41622_(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V<TAB>rose/era/v1_20_1/shim/ItemStackShim.hurtAndBreak<TAB><evidence>`
