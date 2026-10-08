---
id: farmersdelight-51a3199c
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/item/ItemStack.m_41714_(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/world/item/ItemStack;
readable: net/minecraft/world/item/ItemStack.setHoverName(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/world/item/ItemStack;
static: false
newOwner: net/minecraft/world/item/ItemStack
uses: 1
usedIn: vectorwing/farmersdelight/common/block/CookingPotBlock
shimClass: rose.era.v1_20_1.shim.ItemStackShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemStackShim.java
shimSignature: public static net.minecraft.world.item.ItemStack setHoverName(net.minecraft.world.item.ItemStack self, net.minecraft.network.chat.Component component)
ruleLine: net/minecraft/world/item/ItemStack.m_41714_(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/world/item/ItemStack;	rose/era/v1_20_1/shim/ItemStackShim.setHoverName	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/item/ItemStack.setHoverName(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/world/item/ItemStack;`

The mod calls **net/minecraft/world/item/ItemStack.setHoverName(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/world/item/ItemStack;** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `ItemStackShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/ItemStackShim.java`
- Signature: `public static net.minecraft.world.item.ItemStack setHoverName(net.minecraft.world.item.ItemStack self, net.minecraft.network.chat.Component component)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/item/ItemStack.m_41714_(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/world/item/ItemStack;<TAB>rose/era/v1_20_1/shim/ItemStackShim.setHoverName<TAB><evidence>`
