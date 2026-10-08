---
id: farmersdelight-b02b48ce
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_MISSING
symbol: net/minecraft/world/entity/player/Player.m_5661_(Lnet/minecraft/network/chat/Component;Z)V
readable: net/minecraft/world/entity/player/Player.displayClientMessage(Lnet/minecraft/network/chat/Component;Z)V
static: false
newOwner: net/minecraft/world/entity/player/Player
uses: 10
usedIn: vectorwing/farmersdelight/common/block/FeastBlock,vectorwing/farmersdelight/common/block/entity/CuttingBoardBlockEntity,vectorwing/farmersdelight/common/block/entity/SkilletBlockEntity,vectorwing/farmersdelight/common/item/RiceItem,vectorwing/farmersdelight/common/item/SkilletItem
shimClass: rose.era.v1_20_1.shim.PlayerShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/PlayerShim.java
shimSignature: public static void displayClientMessage(net.minecraft.world.entity.player.Player self, net.minecraft.network.chat.Component component, boolean bValue)
ruleLine: net/minecraft/world/entity/player/Player.m_5661_(Lnet/minecraft/network/chat/Component;Z)V	rose/era/v1_20_1/shim/PlayerShim.displayClientMessage	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/entity/player/Player.displayClientMessage(Lnet/minecraft/network/chat/Component;Z)V`

The mod calls **net/minecraft/world/entity/player/Player.displayClientMessage(Lnet/minecraft/network/chat/Component;Z)V** (10 uses), which can't be found in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `PlayerShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/PlayerShim.java`
- Signature: `public static void displayClientMessage(net.minecraft.world.entity.player.Player self, net.minecraft.network.chat.Component component, boolean bValue)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/entity/player/Player.m_5661_(Lnet/minecraft/network/chat/Component;Z)V<TAB>rose/era/v1_20_1/shim/PlayerShim.displayClientMessage<TAB><evidence>`
