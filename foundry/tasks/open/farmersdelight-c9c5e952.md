---
id: farmersdelight-c9c5e952
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/item/ItemStack.m_41700_(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)V
readable: net/minecraft/world/item/ItemStack.addTagElement(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)V
static: false
newOwner: net/minecraft/world/item/ItemStack
uses: 2
usedIn: vectorwing/farmersdelight/common/block/CookingPotBlock,vectorwing/farmersdelight/common/loot/function/CopyMealFunction
shimClass: rose.era.v1_20_1.shim.ItemStackShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemStackShim.java
shimSignature: public static void addTagElement(net.minecraft.world.item.ItemStack self, java.lang.String string, net.minecraft.nbt.Tag tag)
ruleLine: net/minecraft/world/item/ItemStack.m_41700_(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)V	rose/era/v1_20_1/shim/ItemStackShim.addTagElement	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/ItemStack.addTagElement(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)V`

The mod calls **net/minecraft/world/item/ItemStack.addTagElement(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)V** (2 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `ItemStackShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemStackShim.java`
- Signature: `public static void addTagElement(net.minecraft.world.item.ItemStack self, java.lang.String string, net.minecraft.nbt.Tag tag)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/ItemStack.m_41700_(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)V<TAB>rose/era/v1_20_1/shim/ItemStackShim.addTagElement<TAB><evidence>`
