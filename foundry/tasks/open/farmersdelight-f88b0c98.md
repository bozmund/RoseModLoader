---
id: farmersdelight-f88b0c98
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/network/FriendlyByteBuf.m_130267_()Lnet/minecraft/world/item/ItemStack;
readable: net/minecraft/network/FriendlyByteBuf.readItem()Lnet/minecraft/world/item/ItemStack;
static: false
newOwner: net/minecraft/network/FriendlyByteBuf
uses: 3
usedIn: vectorwing/farmersdelight/common/crafting/CookingPotRecipe$Serializer,vectorwing/farmersdelight/common/crafting/ingredient/ChanceResult
shimClass: rose.era.v1_20_1.shim.FriendlyByteBufShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/FriendlyByteBufShim.java
shimSignature: public static net.minecraft.world.item.ItemStack readItem(net.minecraft.network.FriendlyByteBuf self)
ruleLine: net/minecraft/network/FriendlyByteBuf.m_130267_()Lnet/minecraft/world/item/ItemStack;	rose/era/v1_20_1/shim/FriendlyByteBufShim.readItem	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/network/FriendlyByteBuf.readItem()Lnet/minecraft/world/item/ItemStack;`

The mod calls **net/minecraft/network/FriendlyByteBuf.readItem()Lnet/minecraft/world/item/ItemStack;** (3 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `FriendlyByteBufShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/FriendlyByteBufShim.java`
- Signature: `public static net.minecraft.world.item.ItemStack readItem(net.minecraft.network.FriendlyByteBuf self)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/network/FriendlyByteBuf.m_130267_()Lnet/minecraft/world/item/ItemStack;<TAB>rose/era/v1_20_1/shim/FriendlyByteBufShim.readItem<TAB><evidence>`
