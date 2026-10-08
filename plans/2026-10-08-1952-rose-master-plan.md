# Rose Mod Loader: a universal mod loader for Minecraft 26.3

## Context

The goal is to pick any mod from any loader (Forge, Fabric, NeoForge, old FML/ModLoader) and any Minecraft version (1.4.7 → 26.2), drop it into one modpack, and play it on **Minecraft Java 26.3**, the current release (Sept 15, 2026), in singleplayer and multiplayer.

No existing tool does this. The closest projects only cross loaders within the same game version:
- Sinytra Connector (MIT) runs Fabric mods on NeoForge.
- Forgified Fabric API (Apache-2.0) is Fabric API reimplemented on NeoForge.
- Kilt runs Forge mods on Fabric.

Rose goes further and also crosses game versions. The work is too big for one person to write by hand, but most of it is repetitive, verifiable translation work, which suits an AI that runs 24/7. The plan is built around that:

- a **deterministic translation engine** that does everything mechanical;
- a **knowledge base (Rosetta)** that records how each old API maps to 26.3;
- an **AI foundry**: the user's local model, driven by the `pi` agent, takes on the remaining gaps one small, machine-verified task at a time;
- **Claude** handles architecture, review and escalated hard problems.

Every accepted fix becomes a reusable rule, so each mod makes the next one cheaper.

**Decisions made with the user:**
- Name: **Rose Mod Loader** ("Rose" for short). The AI box's endpoint keeps its existing name, `mony`.
- Built **from scratch**: its own launcher, class loading and transformer pipeline. Code from other loaders is borrowed where licenses allow.
- **Multiplayer** is required; both client and server run Rose.
- First pilot: **Farmer's Delight (vectorwing), Forge 1.20.1**. Native 26.3 ports exist (FD Refabricated 3.6.27 for Fabric and an unofficial NeoForge port), and they serve as the answer key.
- Repo: **public GitHub** repo on the user's account at `C:\MyRepositories\Rose`, set up the way Forge, Fabric and NeoForge set theirs up. The repo holds Rose code, mapping data, rules and patches. Game jars, decompiled code and mod jars are downloaded and generated locally and never committed.
- AI: the `pi` agent (0.80.5) runs headless against the `mony` OpenAI-compatible endpoint on a separate AI box.
  - Today that endpoint serves `qwen3.8-27b-q5` (262k context). A 70B+ model is coming, so tasks are sized in tiers.
  - Builds and Minecraft test runs happen on this Windows PC.
- Escalation to Claude: a ticket file in the repo, which the user brings to a Claude Code session.
- The user knows Java but is new to modding. Docs and AGENTS.md must explain Minecraft concepts, not assume them.

---

## 1. Core idea: how an old mod runs on 26.3

An old mod jar touches four kinds of things. Rose translates each one with its own mechanism:

| What the mod touches | Example (FD Forge 1.20.1) | Rose mechanism |
|---|---|---|
| **Vanilla code** (classes, methods, fields) | `ItemStack.getTag()`, `Block.use(...)` | **Era bridge**: renames are remapped automatically, changed semantics go through shim methods, removed classes get re-created shim classes |
| **Loader API** | `@Mod`, `DeferredRegister`, `MinecraftForge.EVENT_BUS`, `SimpleChannel`, `ForgeConfigSpec` | **Dialect**: a reimplementation of that loader+version's API on top of Rose core |
| **Mixins into vanilla** | `farmersdelight.mixins.json` | **Mixin rebaser**: targets are remapped, injection points checked against 26.3, failures repaired by foundry patches |
| **Assets & data** | models, blockstates, recipes, loot tables, tags | **Pack upgrader**: converts JSON formats and folder layout to pack format 121 / resource 97.1, reusing Mojang's DataFixerUpper (MIT) logic where it applies |

### Translation pipeline
The pipeline runs once per mod jar and its output is cached by the jar's SHA-256:

1. **Identify** the source: loader, game version and naming scheme, from `mods.toml`, `fabric.mod.json`, `mcmod.info` or the bytecode itself.
2. **Normalize names.** Old mods are compiled against runtime names, not readable ones:
   - Forge 1.20.1 uses SRG names like `m_41720_`.
   - Fabric uses intermediary names like `method_7909`.
   - Pre-1.13 Forge uses SRG/notch names.

   Rose remaps everything to **Mojang names of the source version**.
3. **Bridge versions.** The Mojang names of the source version are remapped to 26.3 names, which are real names because 26.x ships without obfuscation.
   - Cross-version identity comes from **Fabric intermediary, which keeps the same name for a member from 1.14 through 1.21.11**. That makes "old name → new name" matching free for that whole range.
   - 1.21.11 → 26.x is a direct comparison of Mojang names.
   - Pre-1.14 uses Ornithe (Calamus) and Legacy Fabric intermediaries plus archived MCP mappings.
4. **Apply semantic rules** from Rosetta: call redirects to shims, field accesses turned into accessor calls, class substitutions.
5. **Bridge inheritance.** Mod classes that override vanilla methods whose signature changed get generated bridge methods.

   Example: FD's `CookingPotBlock` overrides 1.20.1 `Block.use(...)`. In 26.3 that call is split into `useItemOn` / `useWithoutItem`. Rose generates both in the mod class and routes them to the old `use`, converting `InteractionResult`.
6. **Rebase Mixins** (section 4).
7. **Upgrade the pack** (assets and data).
8. **Verify statically.** Every remaining reference must resolve against 26.3 plus Rose. Anything unresolved becomes a line in the compatibility report and a foundry task.
9. **Load** through Rose's class loader, with the dialect active.

---

## 2. Architecture and repo layout

Gradle multi-project build, Java 25 (26.3 requires it), Gradle 9.1+.

```
Rose/
  boot/              Launcher + game provider for 26.3 client & dedicated server, version JSON/library resolver
  loader/            Class loader with transformer chain (Knot-like), mod discovery, jar-in-jar, dependency resolution, lifecycle
  mixin-service/     SpongePowered Mixin + MixinExtras service implementation
  api/               Rose's own small native API (the stable core every dialect maps onto)
  core/              Rose's hooks into 26.3 vanilla (via Mixins): registries, events, networking, config, capabilities/attachments
  translate/         The translation engine: remapper, rule interpreter, inheritance bridger, mixin rebaser, static verifier
  rosetta/           Mapping ingest + knowledge base (data files + tools that build it)
  eras/era-1.20.1/   Era bridge: shim classes + rules for vanilla 1.20.1 → 26.3
  dialects/forge-1.20.1/   Forge 1.20.1 API reimplementation (net.minecraftforge.*)
  dialects/fabric-1.20.1/  (later) Fabric Loader 1.20.1 API surface + Fabric API mapping
  packfix/           Asset/data upgrader (DFU-based where possible)
  patches/           Per-mod patches keyed by mod id + jar SHA-256 (small, human-readable)
  analyzer/          CLI: `rose analyze <mod.jar>` → compatibility report (JSON + Markdown)
  testing/           Headless server harness, client smoke harness, GameTests, registry/recipe oracle dumper
  foundry/           AI orchestration (TypeScript, drives `pi --mode rpc`): task queue, context packs, gates, escalation
  agent-bridge/      AI control & testing interface: JSON-RPC, MCP server (`rose mcp`), CLI (`rose ctl`), bots, event stream
  launcher-app/      (later) Modpack builder: pick mods from any version/loader → build a Rose instance
  docs/              ARCHITECTURE.md, ROSETTA.md, DIALECTS.md, MINECRAFT-PRIMER.md, FOUNDRY.md
  AGENTS.md          Rules for the local model (pi reads it automatically) + CLAUDE.md pointer
  plans/             Accepted plans (per the user's global convention)
  corpus/            GIT-IGNORED: game jars per version, decompiled Mojang-named sources, mod jars, native ports for comparison
```

### What to borrow, and the license implications

Rose itself is licensed **LGPL-2.1**, so code from NeoForge/Forge can be adapted. MIT and Apache code is included with its notices.

| Source | License | Borrow for |
|---|---|---|
| Fabric Loader (Knot, game provider, nested jars) | Apache-2.0 | `boot/`, `loader/` design and code |
| SpongePowered Mixin, MixinExtras, Fabric's Mixin fork | MIT | `mixin-service/` |
| ASM, mapping-io | BSD / Apache-2.0 | `translate/`, `rosetta/` |
| tiny-remapper | LGPL-3.0 | used as a library dependency for remapping |
| NeoForge 26.x and Forge 1.20.1 source | LGPL-2.1 | Event bus, registries, config spec, networking, and **where Forge hooks into vanilla**, which the `forge-1.20.1` dialect has to reproduce |
| Sinytra Connector + Forgified Fabric API | MIT / Apache-2.0 | Proven techniques for cross-loader translation; the Fabric dialect later |
| DataFixerUpper + Mojang's fixers | MIT (DFU) / reference | Mapping rules for NBT→components, flattening tables, item/data format upgrades |
| MCPConfig (SRG mappings), Fabric intermediary/Yarn, Ornithe, Legacy Fabric | various open licenses | Rosetta name data |

**Rule:** Mojang's game code is never committed. Like Forge, Fabric and NeoForge, Rose downloads the game and Mojang's metadata at setup time. Decompiled sources live only in `corpus/`.

---

## 3. Rosetta: the knowledge base

Rosetta is the system's memory. It is data in `rosetta/data/`, written in a simple line-based format so diffs stay reviewable. It has three layers:

1. **Name layer** (generated): for each version step, which class, method or field corresponds to which. Built from Mojang mappings (1.14.4+), intermediary, SRG/MCPConfig, Ornithe and Legacy Fabric. Each entry records its evidence and confidence.
2. **Semantic rules** (written by humans or AI, always tested): the translation for things that changed meaning. Rule kinds:
   - `redirect-call`: route an old call to a shim method;
   - `field-to-accessor`;
   - `replace-class`;
   - `bridge-override`: an inheritance bridge template;
   - `mixin-retarget`;
   - `data-transform`.

   Each rule has a unit test and a link to evidence: a Mojang changelog, a NeoForge porting primer, a DFU fixer, or a port-pair diff.
3. **Port-pair evidence** (mined): many popular mods have open-source native ports to newer versions. FD 1.20.1 Forge versus FD Refabricated 26.3 is the first pair. The foundry diffs those sources to learn how humans ported each API change, and turns the patterns into candidate semantic rules. **This is the main thing AI makes possible.**

Rules are written per adjacent version step (1.20.1→1.20.2→…→26.3). Each step is a small diff, which suits the AI. The build **composes** them into a direct `source → 26.3` table. Where composing shim chains gets awkward, a direct shim is written for that era.

Known 1.20.1→26.3 semantic changes that the first era bridge must cover (all of them are FD-relevant):
- Item NBT → **data components** (1.20.5). Shim: a live NBT view of a stack's components. Vanilla keys map through DFU's `ItemStackComponentizationFix` table, and all other keys go to `minecraft:custom_data`, exactly as Mojang's own upgrade does.
- `Block.use` split into `useItemOn` / `useWithoutItem`, with changes to the `InteractionResult` types.
- Registry, id and item property changes: `Item.Properties` needs its id before the item is constructed (1.21.2+).
- Data pack folders renamed to singular (`recipes`→`recipe`, `loot_tables`→`loot_table`, `tags/blocks`→`tags/block`, …), and recipe/ingredient JSON formats changed.
- Item model definitions (`assets/<ns>/items/*.json`, added in 1.21.4) and the rendering/GPU abstraction rewrites affecting block entity renderers.
- Networking moved to `CustomPacketPayload` + `StreamCodec`.
- Renames such as `ResourceLocation` → `Identifier` (late 1.21.x / 26.x).

Each of these is confirmed against the decompiled 26.3 code during Milestone 0, never assumed.

---

## 4. Mixin rebaser

Old mods' Mixins target old method names, signatures and bytecode shapes. In order:

1. Remap the target classes, method selectors and `@Shadow` / `@Accessor` / `@Invoker` members through Rosetta.
2. Validate each injector against the 26.3 bytecode, checking that the target method exists and the `@At` point is still found.
3. When validation passes, keep the Mixin as it is.
4. When it fails, check `patches/<mod>/` for a replacement Mixin written for 26.3. If none exists, the failure becomes a foundry task. Mixins are where most AI effort will go.
5. If a failed Mixin is optional (`require = 0`, or only cosmetic), the mod loads with a warning listed in the report.

---

## 5. The `forge-1.20.1` dialect

This is a reimplementation of the parts of `net.minecraftforge.*` that mods actually use. Scope comes from usage, not from completeness: the analyzer scans a corpus of popular Forge 1.20.1 mods and ranks API members by how many mods use them.

- **Mod loading:** `mods.toml` parsing, `@Mod` entrypoints, `FMLJavaModLoadingContext`, mod event bus vs. Forge event bus, lifecycle events (`FMLCommonSetupEvent`, `FMLClientSetupEvent`, …).
- **Registries:** `DeferredRegister` / `RegistryObject` / `ForgeRegistries` on top of Rose core registries. The 26.3 id-before-construction requirement is handled inside the dialect.
- **Events:** the Forge event bus (adapted from NeoForge's bus). Each Forge event class is fired from a Rose core hook placed where Forge 1.20.1 patched vanilla, using NeoForge 26.x's patch locations as the map.
- **Forge extension methods on vanilla classes** (`IForgeItem`, `IForgeBlock`, `IForgeBlockState`, …): interfaces injected into 26.3 classes with Mixin, plus call sites in vanilla that ask for them.
- **Capabilities:** `LazyOptional` / `ICapabilityProvider` emulated on top of data attachments and block capability lookups.
- **Networking:** `SimpleChannel` encoded as a Rose payload type, plus a handshake that checks mod lists match on both sides.
- **Config:** `ForgeConfigSpec` (adapted from NeoForge `ModConfigSpec`, using NightConfig), synced to clients.
- **Client:** `DistExecutor`, key mappings, `EntityRenderersEvent`, `ModelEvent`, screens and menus.

---

## 6. The AI foundry (the 24/7 engine)

This is a TypeScript orchestrator on this PC. It drives `pi --mode rpc` (provider `mony`), and the model runs on the AI box.

**Task sources** (each task is created automatically, never by hand):
- an unresolved reference in an analyzer report;
- a failed Mixin validation;
- a failing GameTest or oracle diff;
- a crash or error in a smoke-run log;
- a candidate rule mined from a port pair that still needs a test.

**Task format:** `foundry/tasks/<state>/<id>.md`, with YAML frontmatter for kind, mod, tier, attempts and the gates to pass. The states are `open`, `claimed`, `review`, `done` and `escalated`. Tasks are mirrored to GitHub Issues so the user can follow them from anywhere.

**Size tiers:**
- **S:** one rule plus its test, or one shim method. This is the tier for the current 27B model.
- **M:** a class-level shim, or one replacement Mixin.
- **L:** a subsystem slice. Reserved for the 70B+ model or Claude.

The orchestrator only hands out tiers the configured model is allowed to take.

**Context pack per task:** prepared by the orchestrator, so the model doesn't have to search:
- the failing symbol, plus the decompiled old and new vanilla code around it;
- the Agent Bridge operations it may use to reproduce and check the problem in a live game (`rose ctl`);
- relevant port-pair diff hunks;
- the 3 most similar existing Rosetta rules;
- the exact test command;
- the rules from AGENTS.md.

**Execution:**
- Each task runs in its own git worktree on branch `foundry/<id>`.
- pi runs with restricted tools (read/edit/write/bash), and bash is limited to the worktree.
- For isolation, the worker should run in WSL2 or a container with JDK 25 and a Gradle cache, because pi has no safety net of its own.

**Gates** are run by the orchestrator, not the model, so a model can't claim success it didn't earn:
1. Compile and unit tests pass.
2. `rose analyze` shows strictly fewer unresolved items for the target mod, and no regressions on the regression set.
3. The headless 26.3 dedicated server boots with the mod.
4. The relevant GameTests and oracle checks pass.

A task that passes the gates opens a PR. Low-risk kinds (name rules, data transforms with tests) can auto-merge into the integration branch `develop`. Everything else waits for review by the user or Claude.

**Failure handling:** after 3 failed attempts, the context pack is widened. After 5, the task goes to `foundry/escalated/<id>.md`, which records what was tried, the gate output and the model's hypothesis. The user brings that file to a Claude Code session.

**Learning loop:** every merged fix is a Rosetta rule or patch that the analyzer applies automatically to all later mods. Throughput rises over time.

**Telemetry:** `foundry/status.md` is regenerated hourly. It shows tasks per state, mods per compatibility level, gate pass rates and model usage.

---

## 6b. AI-native by design: the Rose Agent Bridge

Rose is a mod loader for the AI age. **AI models are first-class users of the game**, not an add-on. Every capability a human gets through the game window, a model can also get through a structured, scriptable interface. The foundry's own testing runs on this same interface, so it gets exercised all the time.

**Module:** `agent-bridge/`. It's part of Rose core and available on the client, the integrated server and the dedicated server.

**Transports** (one shared set of operations, exposed three ways):
- **JSON-RPC over localhost WebSocket/HTTP:** the base protocol.
- **MCP server** (`rose mcp`): Claude Code and other MCP clients can control the game directly.
- **CLI** (`rose ctl <op> ...`): pi and shell scripts use it through bash, since pi has no MCP support.

**Operations:**
- **Observe:**
  - screenshot (whole screen or the GUI only), camera position, look target;
  - player state (position, health, inventory, effects), nearby blocks and entities as JSON, the open screen with its slots and widgets;
  - chat, recent log lines, the last crash with its full report.
- **Act:**
  - run a command, move or look at a target, walk a path;
  - use or attack, place or break, open a block's GUI, click a slot or button by id, type text, press a key binding.
- **Introspect:**
  - query registries (every block, item, recipe, tag and loot table, including translated mods);
  - list loaded mods with their compatibility level;
  - `explain <symbol>`: which Rosetta rule or shim translated it, and why;
  - the active Mixins and their targets.
- **Scenario and test:**
  - create a world from a template, fix the seed, freeze or step ticks (`tick freeze` / `tick step N`);
  - paste a structure, set up a test area, run GameTests and get structured results;
  - assertions (block at position equals X, slot contains Y);
  - save and restore world snapshots for fast retries.
- **Multi-client:**
  - spawn **bot players**, either fake-connection clients on the server or extra headless clients, so multiplayer and sync tests need no human;
  - each bot has its own observe/act handle.
- **Hot reload (development mode):** reload Rosetta rules, patches and data packs without restarting, so the agent can iterate in seconds.

**Logs and errors are written for models first:**
- Every log line also goes to a JSONL event stream with mod id, phase, and rule/shim id.
- Every crash report gets a "Rose translation context" section: the translated mod frame, the original old symbol, the rule that rewrote it, and links to the matching Rosetta entry. That way a model can go straight from a crash to the rule responsible.

**Machine-readable everywhere:** analyzer reports, compatibility levels, test results and config all have JSON forms. CLI commands support `--json`.

**Safety:**
- The bridge is off in normal play.
- Dev and test profiles enable it, bound to localhost with a per-session token.
- Dedicated servers need an explicit opt-in config, and even then it stays localhost-only.
- Bot and act operations are refused on servers that haven't opted in, so it can't become a cheat client on someone else's server.

**Docs for models:**
- `docs/agent-bridge.md`, plus a schema file listing every operation.
- A pi skill `rose-play-and-test` and the MCP tool descriptions, so any model can learn the interface on its own.

## 7. Verification strategy (the answer key)

All checks below are driven through the Agent Bridge, so a model can run each one, read the result and retry on its own.


- **Oracle comparison.** A separate reference instance runs Fabric 26.3 + FD Refabricated 3.6.27. A dumper mod exports its registries, block properties, recipes, loot tables and tags. The same dumper, as a Rose-native mod, exports the same data from Rose running FD Forge 1.20.1. The differences become a report and tasks. Differences in content the ports changed on purpose are allow-listed.
- **GameTests.** Behavior tests per mod run on a headless server. For FD: the cooking pot cooks a recipe with and without a heat source, the cutting board yields the right outputs, crops grow, the skillet works, and so on.
- **Client smoke test** on this PC: launch the client, create a world, run a scripted check, take screenshots, and fail on any `ERROR` or crash in the log.
- **Multiplayer test:** a dedicated server plus a client on the same machine join, run a sync check (config, custom packets) and leave.
- **Save compatibility:** an FD 1.20.1 world opens with its FD blocks and their block entity contents intact.

### Compatibility levels in reports
- 0: does not load
- 1: loads
- 2: content registered and matches the oracle
- 3: GameTests pass
- 4: multiplayer verified
- 5: played by the user

---

## 8. Roadmap (milestones with exit criteria)

**M0. Groundwork**
- Create `C:\MyRepositories\Rose` and the public GitHub repo. Add the Gradle skeleton, LICENSE (LGPL-2.1), AGENTS.md, docs/MINECRAFT-PRIMER.md, `.gitignore` (excluding `corpus/`), and save this plan to `plans/`.
- Add `corpus` tooling: download 26.3 and 1.20.1 client and server, decompile with Vineflower into Mojang-named source, fetch MCPConfig and intermediary data for 1.20.1 to 1.21.11, and download FD 1.20.1 Forge, the FD 26.3 ports and their sources.
- Confirm the list of 1.20.1→26.3 changes in section 3 against real code.

*Exit:* `./gradlew corpusSetup` reproduces everything from scratch on a clean machine.

**M1. Rose boots vanilla 26.3**
- `boot/` + `loader/` + `mixin-service/` launch the 26.3 client and dedicated server through Rose's class loader.
- A trivial native test mod injects a Mixin and logs.

*Exit:* vanilla is playable through Rose, singleplayer and LAN, and the test mod's Mixin applies on both sides.

**M2. Rose core + native API**
- Registries, an event system, networking (payloads + handshake), config, and data attachments, each through Mixin hooks into 26.3.
- A native sample mod adds a block, an item with a block entity, a recipe and a synced packet.

*Exit:* the sample mod passes GameTests and the multiplayer test.

**M2b. Agent Bridge v1** (before the foundry, because the foundry's gates depend on it)
- JSON-RPC + `rose ctl` CLI + `rose mcp` server.
- Operations: observe, act, introspect, scenario/test, bot players, JSONL event stream, crash translation context, hot reload in dev mode.

*Exit:*
- Claude (through MCP) and pi (through `rose ctl`) can each, with no human help: start a server and client, create a test world, place and use the sample mod's block, read the result, and run its GameTests.
- A bot player joins and verifies a synced packet.

**M3. Translation engine + analyzer + Rosetta v0**
- Name layer for 1.20.1→26.3.
- Remapper, rule interpreter, inheritance bridger, Mixin validator and the `rose analyze` report.

*Exit:* `rose analyze FD-1.20.1.jar` produces a complete list of unresolved items, and pure renames resolve automatically.

**M4. Foundry v1** (runs alongside M5 onward)
- Orchestrator, task generation from the analyzer, context packs, gates, worktrees, escalation tickets and the status page.

*Exit:* the foundry closes a batch of real S-tier tasks with no human help, and the gates reject a deliberately broken fix.

**M5. Forge 1.20.1 dialect + era 1.20.1 bridge → Farmer's Delight**
- Dialect (scope taken from FD plus a ranked usage scan), semantic rules, Mixin rebasing, packfix.

*Exit:* FD Forge 1.20.1 reaches **level 4**: oracle diff clean except allow-listed items, FD GameTests passing, multiplayer verified.

**M6. Widen the Forge 1.20.1 set.** A ladder of roughly 20 popular open-source Forge 1.20.1 mods, ordered from easy to hard: small content mods → mods with GUIs and menus → mods with worldgen → library mods (e.g. GeckoLib, Curios) → heavy tech mods. Create stays last.

*Exit:* the agreed list reaches level ≥3.

**M7. Modpack builder.** The user picks mod jars or Modrinth/CurseForge projects for any supported source. The builder resolves dependencies, runs translation, shows compatibility levels and produces a launchable instance. Integration with the user's launcher (Prism or similar) comes through an instance export.

**M8. Fabric 1.20.1 dialect.** The Fabric Loader API surface, plus Fabric API mapped onto Rose core, borrowing from Forgified Fabric API.

*Exit:* FD Refabricated 1.20.1 and a few Fabric-only mods reach level ≥3.

**M9 and later. Era expansion, one era at a time.** Each era adds a name layer, an era bridge and a dialect where needed. The foundry does most of the work.
- 1.21.1 NeoForge (close to 26.3, mostly cheap).
- 1.19.2 / 1.18.2 Forge and Fabric.
- 1.16.5 Forge (needs a pre-1.17 rendering bridge).
- **1.12.2 Forge.** Several large new problems arrive here:
  - pre-Flattening numeric block IDs + metadata, emulated using DFU's flattening tables;
  - LaunchWrapper and FML coremods (raw ASM patches against obfuscated names, which have to be re-expressed per mod as Rose Mixins);
  - fixed-function OpenGL (`GL11` immediate mode), which needs a GL-emulation layer on 26.3's renderer.
- 1.7.10 Forge.
- **1.4.7 FML/ModLoader** (the user's example). It reuses the pre-Flattening and GL work from 1.12.2 and adds old FML, `mcmod.info` and the even older world and item model.

Each era has the same exit criterion: a chosen mod set for that era reaches level ≥3.

---

## 9. Immediate next steps after approval
1. Move the session to `C:\MyRepositories\Rose` (creating it), run `git init`, and save this plan to `plans/<timestamp>-rose-master-plan.md`.
2. Check the toolchain: JDK 25, Gradle 9.1+, `gh` authenticated as the user, Vineflower available. Create the public GitHub repo `RoseModLoader`.
3. Build M0 (skeleton, docs, corpus tooling), then start M1 (`boot/` launching vanilla 26.3).
4. Write `AGENTS.md` and the first pi skills (`rose-rule-writer`, `rose-mixin-fix`, `rose-packfix`, and `rose-play-and-test` once the Agent Bridge exists) so the local model can start on S-tier tasks as soon as the M3 analyzer produces some.

## 10. Risks, stated plainly
- **Mixins and rendering are where the work piles up.** Expect most foundry effort there. Pre-1.13 GL emulation is a multi-month subsystem.
- **Coremods (1.12.2 and older)** can't be translated automatically. Each one has to be ported by hand, with AI help.
- **Version churn:** each new Minecraft release adds a step to every era. The per-step rule design keeps that to one new step, but someone still has to do it.
- **Model quality:** a 27B model will handle S-tier tasks. M-tier quality depends on the coming 70B+ model, and the escalation path covers the rest.
- **Licensing:** Rose never ships Mojang code or mod jars, and per-mod patches are diffs. Mods with "all rights reserved" licenses still work for personal use, but their patches should stay local (a `patches-local/` folder that is git-ignored).
