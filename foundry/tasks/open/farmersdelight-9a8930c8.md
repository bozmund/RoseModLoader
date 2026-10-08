---
id: farmersdelight-9a8930c8
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/entity/player/StackedContents.m_36466_(Lnet/minecraft/world/item/ItemStack;)V
readable: net/minecraft/world/entity/player/StackedContents.accountSimpleStack(Lnet/minecraft/world/item/ItemStack;)V
static: false
newOwner: net/minecraft/world/entity/player/StackedContents
uses: 1
usedIn: vectorwing/farmersdelight/common/block/entity/container/CookingPotMenu
shimClass: rose.era.v1_20_1.shim.StackedContentsShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/StackedContentsShim.java
shimSignature: public static void accountSimpleStack(net.minecraft.world.entity.player.StackedContents self, net.minecraft.world.item.ItemStack itemStack)
ruleLine: net/minecraft/world/entity/player/StackedContents.m_36466_(Lnet/minecraft/world/item/ItemStack;)V	rose/era/v1_20_1/shim/StackedContentsShim.accountSimpleStack	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/entity/player/StackedContents.accountSimpleStack(Lnet/minecraft/world/item/ItemStack;)V`

The mod calls **net/minecraft/world/entity/player/StackedContents.accountSimpleStack(Lnet/minecraft/world/item/ItemStack;)V** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `StackedContentsShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/StackedContentsShim.java`
- Signature: `public static void accountSimpleStack(net.minecraft.world.entity.player.StackedContents self, net.minecraft.world.item.ItemStack itemStack)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/entity/player/StackedContents.m_36466_(Lnet/minecraft/world/item/ItemStack;)V<TAB>rose/era/v1_20_1/shim/StackedContentsShim.accountSimpleStack<TAB><evidence>`
