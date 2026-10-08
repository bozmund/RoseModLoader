---
id: farmersdelight-83678dd7
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/world/level/Level.m_5594_(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V
readable: net/minecraft/world/level/Level.playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V
static: false
newOwner: net/minecraft/world/level/Level
uses: 18
usedIn: vectorwing/farmersdelight/common/block/AbstractStoveBlock,vectorwing/farmersdelight/common/block/CookingPotBlock,vectorwing/farmersdelight/common/block/FeastBlock,vectorwing/farmersdelight/common/block/MushroomColonyBlock,vectorwing/farmersdelight/common/block/PieBlock,vectorwing/farmersdelight/common/block/SkilletBlock,vectorwing/farmersdelight/common/block/TomatoBlock,vectorwing/farmersdelight/common/item/DogFoodItem$DogFoodEvent,vectorwing/farmersdelight/common/item/HorseFeedItem$HorseFeedEvent,vectorwing/farmersdelight/common/item/KnifeItem$KnifeEvents
shimClass: rose.era.v1_20_1.shim.LevelShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/LevelShim.java
shimSignature: public static void playSound(net.minecraft.world.level.Level self, net.minecraft.world.entity.player.Player player, net.minecraft.core.BlockPos blockPos, net.minecraft.sounds.SoundEvent soundEvent, net.minecraft.sounds.SoundSource soundSource, float fValue, float fValue2)
ruleLine: net/minecraft/world/level/Level.m_5594_(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V	rose/era/v1_20_1/shim/LevelShim.playSound	<evidence>
attempts: 0
created: 2026-10-09
lastFeedback: -
---

# Redirect `net/minecraft/world/level/Level.playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V`

The mod calls **net/minecraft/world/level/Level.playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V** (18 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `LevelShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/LevelShim.java`
- Signature: `public static void playSound(net.minecraft.world.level.Level self, net.minecraft.world.entity.player.Player player, net.minecraft.core.BlockPos blockPos, net.minecraft.sounds.SoundEvent soundEvent, net.minecraft.sounds.SoundSource soundSource, float fValue, float fValue2)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/level/Level.m_5594_(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V<TAB>rose/era/v1_20_1/shim/LevelShim.playSound<TAB><evidence>`

## Closed 2026-10-09 00:38

No longer reported by `rose analyze` (resolved elsewhere).
