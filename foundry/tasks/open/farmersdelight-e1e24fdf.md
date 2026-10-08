---
id: farmersdelight-e1e24fdf
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: SIGNATURE_CHANGED
symbol: net/minecraft/core/Registry.m_7745_(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;
readable: net/minecraft/core/Registry.get(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;
static: false
newOwner: net/minecraft/core/Registry
uses: 1
usedIn: vectorwing/farmersdelight/common/world/VillageStructures
shimClass: rose.era.v1_20_1.shim.RegistryShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/RegistryShim.java
shimSignature: public static java.lang.Object get(net.minecraft.core.Registry self, net.minecraft.resources.Identifier identifier)
ruleLine: net/minecraft/core/Registry.m_7745_(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;	rose/era/v1_20_1/shim/RegistryShim.get	<evidence>
attempts: 0
created: 2026-10-09
closedReason: -
---

# Redirect `net/minecraft/core/Registry.get(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;`

The mod calls **net/minecraft/core/Registry.get(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;** (1 uses), which still exists by name but with a different signature in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `RegistryShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/RegistryShim.java`
- Signature: `public static java.lang.Object get(net.minecraft.core.Registry self, net.minecraft.resources.Identifier identifier)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/core/Registry.m_7745_(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;<TAB>rose/era/v1_20_1/shim/RegistryShim.get<TAB><evidence>`

## Closed 2026-10-09 00:38

No longer reported by `rose analyze` (resolved elsewhere).

## Reopened 2026-10-09 00:39

Reported again by `rose analyze`.
