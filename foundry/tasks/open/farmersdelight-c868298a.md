---
id: farmersdelight-c868298a
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/core/RegistryAccess.m_6632_(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;
readable: net/minecraft/core/RegistryAccess.registry(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;
static: false
newOwner: net/minecraft/core/RegistryAccess
uses: 1
usedIn: vectorwing/farmersdelight/common/block/SandyShrubBlock
shimClass: rose.era.v1_20_1.shim.RegistryAccessShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/RegistryAccessShim.java
shimSignature: public static java.util.Optional registry(net.minecraft.core.RegistryAccess self, net.minecraft.resources.ResourceKey resourceKey)
ruleLine: net/minecraft/core/RegistryAccess.m_6632_(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;	rose/era/v1_20_1/shim/RegistryAccessShim.registry	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/core/RegistryAccess.registry(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;`

The mod calls **net/minecraft/core/RegistryAccess.registry(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `RegistryAccessShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/RegistryAccessShim.java`
- Signature: `public static java.util.Optional registry(net.minecraft.core.RegistryAccess self, net.minecraft.resources.ResourceKey resourceKey)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/core/RegistryAccess.m_6632_(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;<TAB>rose/era/v1_20_1/shim/RegistryAccessShim.registry<TAB><evidence>`
