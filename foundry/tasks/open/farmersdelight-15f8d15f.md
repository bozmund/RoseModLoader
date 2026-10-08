---
id: farmersdelight-15f8d15f
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/world/level/storage/loot/LootContext.m_78953_(Lnet/minecraft/world/level/storage/loot/parameters/LootContextParam;)Ljava/lang/Object;
readable: net/minecraft/world/level/storage/loot/LootContext.getParamOrNull(Lnet/minecraft/world/level/storage/loot/parameters/LootContextParam;)Ljava/lang/Object;
static: false
newOwner: net/minecraft/world/level/storage/loot/LootContext
uses: 3
usedIn: vectorwing/farmersdelight/common/loot/function/CopyMealFunction,vectorwing/farmersdelight/common/loot/function/CopySkilletFunction,vectorwing/farmersdelight/common/loot/modifier/PastrySlicingModifier
shimClass: rose.era.v1_20_1.shim.LootContextShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/LootContextShim.java
shimSignature: public static java.lang.Object getParamOrNull(net.minecraft.world.level.storage.loot.LootContext self, net.minecraft.util.context.ContextKey contextKey)
ruleLine: net/minecraft/world/level/storage/loot/LootContext.m_78953_(Lnet/minecraft/world/level/storage/loot/parameters/LootContextParam;)Ljava/lang/Object;	rose/era/v1_20_1/shim/LootContextShim.getParamOrNull	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/world/level/storage/loot/LootContext.getParamOrNull(Lnet/minecraft/world/level/storage/loot/parameters/LootContextParam;)Ljava/lang/Object;`

The mod calls **net/minecraft/world/level/storage/loot/LootContext.getParamOrNull(Lnet/minecraft/world/level/storage/loot/parameters/LootContextParam;)Ljava/lang/Object;** (3 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `LootContextShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/LootContextShim.java`
- Signature: `public static java.lang.Object getParamOrNull(net.minecraft.world.level.storage.loot.LootContext self, net.minecraft.util.context.ContextKey contextKey)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/world/level/storage/loot/LootContext.m_78953_(Lnet/minecraft/world/level/storage/loot/parameters/LootContextParam;)Ljava/lang/Object;<TAB>rose/era/v1_20_1/shim/LootContextShim.getParamOrNull<TAB><evidence>`
