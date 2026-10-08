# Rose architecture

The full design and its reasoning are in the master plan in `plans/`. This page is the short map.

## How an old mod runs on 26.3

An old mod jar goes through the translation pipeline once. The result is cached by the jar's SHA-256.

1. **Identify** the source loader and game version (`mods.toml`, `fabric.mod.json`, `mcmod.info`).
2. **Normalize names**: SRG (Forge) or intermediary (Fabric) → Mojang names of the source version.
3. **Bridge versions**: source Mojang names → 26.3 names, using Rosetta's name layer.
4. **Apply semantic rules** for APIs that changed meaning: call redirects to shims, class replacements.
5. **Bridge inheritance**: generated methods for overridden vanilla methods whose signature changed.
6. **Rebase Mixins** onto 26.3 and validate them. Any that fail become foundry tasks.
7. **Upgrade assets/data** to 26.3 formats (packfix).
8. **Verify statically**: anything unresolved goes into the compatibility report.
9. **Load** through Rose's class loader, with the matching dialect (e.g. Forge 1.20.1) active.

## Modules

| Module | Package | Role |
|---|---|---|
| `boot` | `rose.boot` | Launches the 26.3 client / dedicated server through Rose |
| `loader` | `rose.loader` | Class loader + transformer chain, mod discovery, lifecycle |
| `mixin-service` | `rose.mixin` | Mixin integration |
| `api` | `rose.api` | Rose's small native API |
| `core` | `rose.core` | Rose's hooks into 26.3: registries, events, networking, config |
| `translate` | `rose.translate` | Remapper, rule interpreter, inheritance bridger, Mixin rebaser |
| `rosetta` | `rose.rosetta` | Knowledge base: name layer, semantic rules, port-pair evidence |
| `eras/era-1.20.1` | `rose.era.v1_20_1` | Shims for vanilla 1.20.1 → 26.3 |
| `dialects/forge-1.20.1` | `rose.dialect.forge.v1_20_1` | Forge 1.20.1 API reimplementation |
| `packfix` | `rose.packfix` | Asset/data pack upgrader |
| `analyzer` | `rose.analyzer` | `rose analyze <mod.jar>` compatibility report |
| `agent-bridge` | `rose.bridge` | AI control & testing interface (JSON-RPC, MCP, CLI) |
| `testing` | `rose.testing` | Server/client harnesses, GameTests, oracle dumper |
| `corpus-tools` | `rose.corpus` | Builds the local `corpus/` (game jars, mappings, decompiled source) |
| `foundry/` (TypeScript, later) | | 24/7 AI task orchestrator driving the `pi` agent |

## The corpus (local only)

`./gradlew corpusSetup` creates `corpus/minecraft/<version>/` containing:

- `version.json`, `client.jar`, `server-bundler.jar`, `server.jar`: Mojang's files, SHA-1 verified;
- `client-mappings.txt` / `server-mappings.txt`: Mojang's official mappings (obfuscated versions only);
- `client-named.jar`: the client with real Mojang names;
- `src/`: decompiled source of the named jar.

None of this may be committed.
