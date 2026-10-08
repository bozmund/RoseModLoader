---
id: farmersdelight-a338e44b
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/item/enchantment/EnchantmentHelper.m_44843_(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/item/ItemStack;)I
readable: net/minecraft/world/item/enchantment/EnchantmentHelper.getItemEnchantmentLevel(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/item/ItemStack;)I
static: true
newOwner: net/minecraft/world/item/enchantment/EnchantmentHelper
uses: 2
usedIn: vectorwing/farmersdelight/common/item/SkilletItem,vectorwing/farmersdelight/common/item/enchantment/BackstabbingEnchantment$BackstabbingEvent
shimClass: rose.era.v1_20_1.shim.EnchantmentHelperShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/EnchantmentHelperShim.java
shimSignature: public static int getItemEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantment enchantment, net.minecraft.world.item.ItemStack itemStack)
ruleLine: net/minecraft/world/item/enchantment/EnchantmentHelper.m_44843_(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/item/ItemStack;)I	rose/era/v1_20_1/shim/EnchantmentHelperShim.getItemEnchantmentLevel	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/enchantment/EnchantmentHelper.getItemEnchantmentLevel(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/item/ItemStack;)I`

The mod calls **net/minecraft/world/item/enchantment/EnchantmentHelper.getItemEnchantmentLevel(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/item/ItemStack;)I** (2 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `EnchantmentHelperShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/EnchantmentHelperShim.java`
- Signature: `public static int getItemEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantment enchantment, net.minecraft.world.item.ItemStack itemStack)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/enchantment/EnchantmentHelper.m_44843_(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/item/ItemStack;)I<TAB>rose/era/v1_20_1/shim/EnchantmentHelperShim.getItemEnchantmentLevel<TAB><evidence>`
