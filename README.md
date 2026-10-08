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
| M2 | Rose core + native API | |
| M2b | Agent Bridge v1 | |
| M3 | Translation engine, analyzer, Rosetta v0 | |
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
```

## Writing a Rose-native mod (early)

Put a `rose.mod.json` at the root of your jar:

```json
{
  "schemaVersion": 1,
  "id": "hello",
  "version": "0.0.1",
  "mixins": ["hello.mixins.json"]
}
```

Drop the jar into `run/client/mods/` or `run/server/mods/`. Mixin (with MixinExtras) is available. Since Minecraft 26.x isn't obfuscated, no refmap is needed. See [`testmods/hello`](testmods/hello) for a working example.

## Legal

Rose never includes or redistributes Minecraft's code or other people's mods. As with Forge, Fabric and NeoForge, game files are downloaded from Mojang on your machine, and the `corpus/` folder is git-ignored. Rose is licensed under the [LGPL-2.1](LICENSE).

Rose is not an official Minecraft product and is not approved by or associated with Mojang or Microsoft.
