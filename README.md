# Rose Mod Loader

**Play mods from any loader and any Minecraft version on Minecraft 26.3.**

Rose is a mod loader for Minecraft Java 26.3. Its goal is to load mods built for other loaders (Forge, Fabric, NeoForge, old FML) and other game versions (back to 1.4.7) by translating them at load time:

- **Era bridges** translate calls to old vanilla code into 26.3 code.
- **Dialects** reimplement other loaders' APIs (e.g. Forge 1.20.1) on top of Rose.
- **The Mixin rebaser** retargets old mods' Mixins at 26.3.
- **Packfix** upgrades old assets and data to 26.3 formats.
- **Rosetta**, a knowledge base, records how each old API maps to 26.3. It grows with every mod that gets fixed.

Rose is built to be **AI-native**. The **Rose Agent Bridge** lets AI models observe, control and test the game through MCP, JSON-RPC or a CLI. An **AI foundry** runs a local model around the clock to close compatibility gaps, one small machine-verified task at a time.

> **Status: early development.** Nothing is playable yet. See the [master plan](plans/) and the roadmap below.

## Roadmap

| Milestone | Goal | Status |
|---|---|---|
| M0 | Groundwork: repo, build, corpus tooling | done |
| M1 | Rose boots vanilla 26.3 (client + server) with Mixin | done (LAN check pending) |
| M2 | Rose core + native API | done |
| M2b | Agent Bridge v1: `rose` CLI + MCP server | done (dedicated-server run needs EULA) |
| M3 | Translation engine, analyzer, Rosetta v0 | done: `rose analyze` lists every gap for Farmer's Delight 1.20.1 |
| M4 | AI foundry v1 | |
| M5 | Forge 1.20.1 dialect → **Farmer's Delight 1.20.1 on 26.3** | |
| M6+ | More Forge 1.20.1 mods, modpack builder, Fabric 1.20.1, older eras | |

## Building

Requirements: JDK 25. The Gradle wrapper is included.

```bash
./gradlew build
./gradlew corpusSetup   # downloads Minecraft 26.3 + 1.20.1 into corpus/ and decompiles them (local only)
./gradlew runClient     # starts the 26.3 client through Rose (offline dev account), with the test mods
./gradlew runServer     # starts the dedicated server; accept Mojang's EULA in run/server/eula.txt first
./gradlew runGameTests  # runs mods' GameTests headless; fails if any required test fails (report: build/gametest/report.xml)
./gradlew corpusInputs  # mapping files + pilot mods for Rosetta (local only)
./rose analyze corpus/mods/FarmersDelight-1.20.1-1.3.4.jar   # what stands between an old mod and 26.3
```

See [docs/ROSETTA.md](docs/ROSETTA.md) for how translation and the compatibility report work.

## Letting AI drive the game

```bash
./rose launch client               # rose.cmd on Windows
./rose ctl client.createTestWorld
./rose ctl world.setBlock x=10 y=4 z=-6 block=sample:counter_block
./rose ctl client.useBlock x=10 y=4 z=-6
./rose events --types overlay      # -> "Counter: 1"
./rose test "sample:*"             # GameTests, headless
```

Claude Code picks up the `rose` MCP server from `.mcp.json`. It gets tools to launch, call any bridge method, read events, take screenshots (returned as images) and run tests. See [docs/agent-bridge.md](docs/agent-bridge.md).

## Writing a Rose-native mod (early)

Put a `rose.mod.json` at the root of your jar:

```json
{
  "schemaVersion": 1,
  "id": "sample",
  "version": "0.0.1",
  "mixins": ["sample.mixins.json"],
  "entrypoints": {
    "main": ["com.example.SampleMod"],
    "client": ["com.example.SampleClient"]
  }
}
```

Drop the jar into `run/client/mods/` or `run/server/mods/`. Your jar's `data/` and `assets/` folders load automatically as built-in packs (no `pack.mcmeta` needed).

| API | What it does |
|---|---|
| `rose.api.ModInitializer` / `rose.api.client.ClientModInitializer` | entrypoints (`main` runs just before registries freeze) |
| `rose.api.registry.RoseRegistries` | blocks, items, block items, block entity types (sets the registry key for you) |
| `rose.api.event.RoseEvents` | server started/stopping/tick, player join |
| `rose.api.network.RoseNetworking` (+ `client.RoseClientNetworking`) | custom packets in both directions |
| `rose.api.config.RoseConfig` | per-mod JSON config in `config/<modid>.json` |
| `rose.api.gametest.RoseGameTests` | GameTest functions |

Mixin (with MixinExtras) is available. Since Minecraft 26.x isn't obfuscated, no refmap is needed. Examples: [`testmods/sample`](testmods/sample) (all of the above) and [`testmods/hello`](testmods/hello) (Mixin only).

## Legal

Rose never includes or redistributes Minecraft's code or other people's mods. As with Forge, Fabric and NeoForge, game files are downloaded from Mojang on your machine, and the `corpus/` folder is git-ignored. Rose is licensed under the [LGPL-2.1](LICENSE).

Rose is not an official Minecraft product and is not approved by or associated with Mojang or Microsoft.
