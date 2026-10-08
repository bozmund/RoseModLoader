---
id: farmersdelight-52bf906f
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/core/Registry.m_246971_(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/core/Holder$Reference;
readable: net/minecraft/core/Registry.getHolderOrThrow(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/core/Holder$Reference;
static: false
newOwner: net/minecraft/core/Registry
uses: 2
usedIn: vectorwing/farmersdelight/common/registry/ModDamageTypes,vectorwing/farmersdelight/common/world/VillageStructures
shimClass: rose.era.v1_20_1.shim.RegistryShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/RegistryShim.java
shimSignature: public static net.minecraft.core.Holder.Reference getHolderOrThrow(net.minecraft.core.Registry self, net.minecraft.resources.ResourceKey resourceKey)
ruleLine: net/minecraft/core/Registry.m_246971_(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/core/Holder$Reference;	rose/era/v1_20_1/shim/RegistryShim.getHolderOrThrow	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/core/Registry.getHolderOrThrow(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/core/Holder$Reference;`

The mod calls **net/minecraft/core/Registry.getHolderOrThrow(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/core/Holder$Reference;** (2 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `RegistryShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/RegistryShim.java`
- Signature: `public static net.minecraft.core.Holder.Reference getHolderOrThrow(net.minecraft.core.Registry self, net.minecraft.resources.ResourceKey resourceKey)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/core/Registry.m_246971_(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/core/Holder$Reference;<TAB>rose/era/v1_20_1/shim/RegistryShim.getHolderOrThrow<TAB><evidence>`
