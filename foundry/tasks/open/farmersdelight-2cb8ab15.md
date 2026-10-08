---
id: farmersdelight-2cb8ab15
kind: redirect
tier: S
mod: farmersdelight
jar: corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
finding: METHOD_GONE
symbol: net/minecraft/core/Registry.m_203636_(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;
readable: net/minecraft/core/Registry.getHolder(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;
static: false
newOwner: net/minecraft/core/Registry
uses: 1
usedIn: vectorwing/farmersdelight/common/block/SandyShrubBlock
shimClass: rose.era.v1_20_1.shim.RegistryShim
shimFile: eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/RegistryShim.java
shimSignature: public static java.util.Optional getHolder(net.minecraft.core.Registry self, net.minecraft.resources.ResourceKey resourceKey)
ruleLine: net/minecraft/core/Registry.m_203636_(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;	rose/era/v1_20_1/shim/RegistryShim.getHolder	<evidence>
attempts: 0
created: 2026-10-09
---

# Redirect `net/minecraft/core/Registry.getHolder(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;`

The mod calls **net/minecraft/core/Registry.getHolder(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;** (1 uses), which no longer exists in Minecraft 26.3.
Write a static shim that does what the 1.20.1 method did, using 26.3 APIs, and register it with one rule line.

- Shim: `RegistryShim` in `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/RegistryShim.java`
- Signature: `public static java.util.Optional getHolder(net.minecraft.core.Registry self, net.minecraft.resources.ResourceKey resourceKey)`
- Rule (append to `rosetta/rules/forge-1.20.1/redirects.tsv`, replace `<evidence>`):
  `net/minecraft/core/Registry.m_203636_(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;<TAB>rose/era/v1_20_1/shim/RegistryShim.getHolder<TAB><evidence>`
