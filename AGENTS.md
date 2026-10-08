# AGENTS.md: rules for AI agents working on Rose

You are working on **Rose Mod Loader**, a Minecraft Java 26.3 mod loader that runs mods from other loaders and older game versions by translating them. Read `docs/ARCHITECTURE.md` first. If Minecraft modding is new to you, read `docs/MINECRAFT-PRIMER.md`.

## Hard rules
1. **Never commit Minecraft game code, decompiled sources, or mod jars.** They live in `corpus/` (git-ignored). Quote at most a few lines in comments or docs, only when needed to explain a rule.
2. **Never weaken a test or a gate to make it pass.** If a test is wrong, say so in your task report instead of changing it.
3. **Stay inside your task.** One task = one focused change. If you find another problem, write it in your task report under "Follow-ups"; don't fix it.
4. **Evidence for every translation rule.** Each Rosetta rule needs a unit test and an `evidence:` line pointing to its source: a Mojang changelog, a NeoForge/Fabric porting primer, a DataFixerUpper fixer, or a port-pair diff.
5. **Check the real code.** Look up vanilla classes in `corpus/minecraft/<version>/src/`. Don't guess method names or signatures from memory; they change between versions.
6. **Gates decide success, not you.** The foundry orchestrator runs the build, tests, analyzer and game tests after you finish. Your report should say what you changed and why it should pass.

## Project facts
- Java 25, Gradle (use `./gradlew`), multi-project build. Packages start with `rose.`.
- Target game: Minecraft **26.3**. It is unobfuscated, so class and method names in `corpus/minecraft/26.3/src` are Mojang's real names.
- Old versions such as 1.20.1 are obfuscated. Rose remaps them to Mojang names in `corpus/minecraft/1.20.1/client-named.jar` and decompiles them to `.../src`.
- Module map: see `docs/ARCHITECTURE.md`.

## Code style
- Match surrounding code. Keep comments short and only where they explain *why*.
- Small classes, clear names, no speculative abstractions.
- Tests use JUnit (`src/test/java`).

## Commands
- Build everything: `./gradlew build`
- One module's tests: `./gradlew :translate:test`
- Prepare the corpus: `./gradlew corpusSetup`
- In-game tests, headless, no EULA needed: `./gradlew runGameTests` (select with `-Prose.tests=sample:*`). The task fails if any required GameTest fails, and the report is written to `build/gametest/report.xml`.

## Seeing and driving the game (Agent Bridge)
You can run the game and check your change yourself. Full reference: `docs/agent-bridge.md`.
- `./rose launch client` (Windows: `rose.cmd`), then `./rose ctl client.createTestWorld`.
- `./rose methods` lists everything you can call. Use `./rose ctl <method> key=value ...` to call one.
- Read what happened with `./rose events --types log,chat,overlay`, and look with `./rose ctl client.screenshot`.
- Always finish with `./rose stop client`.
- For pass/fail evidence, prefer GameTests (`./rose test "modid:*"`) over manual clicking: they are what the gates run.

## Getting stuck
If you can't make progress after a real attempt, stop and write an escalation note in your task report:
- what you tried;
- the exact error or gate output;
- your best hypothesis.

A human or a stronger model will pick it up.
