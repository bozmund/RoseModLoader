---
id: farmersdelight-c7f8184c
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/entity/player/Player.m_21051_(Lnet/minecraft/world/entity/ai/attributes/Attribute;)Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;
readable: net/minecraft/world/entity/player/Player.getAttribute(Lnet/minecraft/world/entity/ai/attributes/Attribute;)Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;
static: false
newOwner: net/minecraft/world/entity/player/Player
uses: 1
usedIn: vectorwing/farmersdelight/client/gui/ComfortHealthOverlay
shimClass: rose.era.v1_20_1.shim.PlayerShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/PlayerShim.java
shimSignature: public static net.minecraft.world.entity.ai.attributes.AttributeInstance getAttribute(net.minecraft.world.entity.player.Player self, net.minecraft.world.entity.ai.attributes.Attribute attribute)
ruleLine: net/minecraft/world/entity/player/Player.m_21051_(Lnet/minecraft/world/entity/ai/attributes/Attribute;)Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;	rose/era/v1_20_1/shim/PlayerShim.getAttribute	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/entity/player/Player.getAttribute(Lnet/minecraft/world/entity/ai/attributes/Attribute;)Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;`

The mod calls **net/minecraft/world/entity/player/Player.getAttribute(Lnet/minecraft/world/entity/ai/attributes/Attribute;)Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;** (1 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `PlayerShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/PlayerShim.java`
- Signature: `public static net.minecraft.world.entity.ai.attributes.AttributeInstance getAttribute(net.minecraft.world.entity.player.Player self, net.minecraft.world.entity.ai.attributes.Attribute attribute)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/entity/player/Player.m_21051_(Lnet/minecraft/world/entity/ai/attributes/Attribute;)Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;<TAB>rose/era/v1_20_1/shim/PlayerShim.getAttribute<TAB><evidence>`
