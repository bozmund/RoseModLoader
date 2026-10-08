# Minecraft modding primer (for Rose contributors)

This is the minimum you need to know about Minecraft internals to work on Rose.

## Game versions and names
- Minecraft Java runs on the JVM. **Before 26.1, Mojang obfuscated the game**, so classes shipped with names like `a`, `bcd`. Since 1.14.4 Mojang also publishes official **mappings** that restore real names. Rose calls these "Mojang names".
- Modding tools invented their own intermediate naming schemes, and mods are compiled against them:
  - **SRG / MCP** (Forge), e.g. `m_41720_`. Forge 1.20.1 mods use SRG names at runtime.
  - **Intermediary** (Fabric), e.g. `method_7909`. Each name stays the same from 1.14 through 1.21.11, which makes it ideal for matching the same method across versions.
  - **Mojang names** (NeoForge 1.20.2+, and every 26.x version, since those ship unobfuscated).
- Version numbering changed in 2026: after 1.21.11 came 26.1, 26.2, 26.3 (year-based).

## Loaders
- **Forge**: patches vanilla classes directly and adds a big API: an event bus, `DeferredRegister`, capabilities, `SimpleChannel` networking. Mods declare themselves in `META-INF/mods.toml` and have an `@Mod` class.
- **NeoForge**: the 2023 fork of Forge and its successor. It's similar, but many APIs have been renamed or redesigned since.
- **Fabric**: a minimal loader. Mods change the game mainly through **Mixins**. Most API comes from the separate *Fabric API* mod. Mods declare themselves in `fabric.mod.json`.

## Mixins
A Mixin is a class whose code gets merged into a Minecraft class at load time, for example "inject this code at the start of `Block.use`". Mixins name their target methods and code locations exactly, so they break when Minecraft changes. That's why Rose has a Mixin rebaser.

## Registries
Every block, item, entity type, sound and so on is registered under an id such as `farmersdelight:cooking_pot`. Mods register their content during startup, and Rose has to make old registration code work with 26.3's registries.

## Client vs. server
Even singleplayer runs an internal server. Game logic runs on the server, while rendering, input and GUIs run on the client. Mods often have code that may only run on one side. In multiplayer, both sides need Rose and the same mods.

## Data and assets
- **Assets** (client): textures, models, sounds, translations → `assets/<namespace>/...`
- **Data** (server): recipes, loot tables, tags, worldgen → `data/<namespace>/...`

Their formats change between versions. Packfix upgrades them.

## Big changes between 1.20.1 and 26.3 (from the plan; verify against the corpus)
- Item NBT tags were replaced by **data components** (1.20.5).
- `Block.use` was split into `useItemOn` / `useWithoutItem` (1.20.5).
- Items must know their registry id when they're constructed (1.21.2).
- Data pack folders became singular (`recipes` → `recipe`, ...), and recipe formats changed (1.21).
- Item model definitions moved into `assets/<ns>/items/` (1.21.4), and rendering was reworked.
- Custom networking uses `CustomPacketPayload` + `StreamCodec` (1.20.5).
- `ResourceLocation` was renamed to `Identifier` (late 1.21.x).
- Window and input moved from GLFW to **SDL3** (26.3). Mouse buttons now use SDL numbering: **left = 1** (`InputConstants.MOUSE_BUTTON_LEFT`), where GLFW-era code used 0. Old mods that compare mouse button numbers will need an era-bridge rule. (Found while building the Agent Bridge: a click with button 0 is silently ignored.)
