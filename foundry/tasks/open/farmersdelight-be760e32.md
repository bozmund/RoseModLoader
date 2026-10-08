---
id: farmersdelight-be760e32
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/item/enchantment/EnchantmentHelper.m_44938_(Lnet/minecraft/world/entity/LivingEntity;)Z
readable: net/minecraft/world/item/enchantment/EnchantmentHelper.hasFrostWalker(Lnet/minecraft/world/entity/LivingEntity;)Z
static: true
newOwner: net/minecraft/world/item/enchantment/EnchantmentHelper
uses: 1
usedIn: vectorwing/farmersdelight/common/block/AbstractStoveBlock
shimClass: rose.era.v1_20_1.shim.EnchantmentHelperShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/EnchantmentHelperShim.java
shimSignature: public static boolean hasFrostWalker(net.minecraft.world.entity.LivingEntity livingEntity)
ruleLine: net/minecraft/world/item/enchantment/EnchantmentHelper.m_44938_(Lnet/minecraft/world/entity/LivingEntity;)Z	rose/era/v1_20_1/shim/EnchantmentHelperShim.hasFrostWalker	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/enchantment/EnchantmentHelper.hasFrostWalker(Lnet/minecraft/world/entity/LivingEntity;)Z`

The mod calls **net/minecraft/world/item/enchantment/EnchantmentHelper.hasFrostWalker(Lnet/minecraft/world/entity/LivingEntity;)Z** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `EnchantmentHelperShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/EnchantmentHelperShim.java`
- Signature: `public static boolean hasFrostWalker(net.minecraft.world.entity.LivingEntity livingEntity)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/enchantment/EnchantmentHelper.m_44938_(Lnet/minecraft/world/entity/LivingEntity;)Z<TAB>rose/era/v1_20_1/shim/EnchantmentHelperShim.hasFrostWalker<TAB><evidence>`
