# The AI foundry

The foundry turns `rose analyze` findings into small tasks and works through them with an AI agent. Nothing lands unless automated gates prove it. It's designed to run unattended with a local model through the `pi` coding agent, and to hand anything it can't finish to a stronger model or a human.

```
rose analyze mod.jar ──▶ rose foundry plan ──▶ foundry/tasks/open/*.md
                                                     │  (most-used symbols first, one at a time)
                                                     ▼
                         worktree foundry/worktrees/<id>  (branch foundry/<id> from develop,
                                                     │       linked to the shared corpus/ and run/)
                         context pack .foundry/TASK.md ──▶ agent (pi + local model, or a script)
                                                     ▼
                         gates: scope ▸ build ▸ analyze delta ▸ GameTests
                           pass ──▶ commit, land on `develop` (fast-forward) ──▶ tasks/landed
                           fail ──▶ feedback to the agent, retry ──(5 attempts)──▶ tasks/escalated (ticket)
```

## Commands

```bash
./rose analyze corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
./rose foundry plan build/analyze/FarmersDelight-1.20.1-1.3.4.rose.json corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
./rose foundry status                       # also written to foundry/status.md
./rose foundry pack <task-id>               # read exactly what the agent will get
./rose foundry run --max 10                 # pi with the local "mony" provider (default)
./rose foundry run --max 10 --model mony/qwen3.8-27b-q5 --thinking high
./rose foundry run --task <id> --agent script:path/to/agent.sh
./rose foundry retry <id>                   # escalated -> open again
./rose foundry reject <id> <reason>
```

For 24/7 operation, loop `rose foundry run --max 20` with a pause in between (Task Scheduler, cron, or a shell loop). Each run picks up where the last one stopped.

## Task kinds (v1)

| Kind | Tier | What the agent writes | Allowed files |
|---|---|---|---|
| `redirect` | S | one static shim with an exact, given signature, plus one rule line with evidence | `eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/*.java`, `rosetta/rules/forge-1.20.1/redirects.tsv` |

Inheritance bridges (`OVERRIDE_*`), Forge dialect classes, field accessors, constructors and Mixin retargets come next. Each new kind needs a planner, an allowed-file scope and gates.

## The context pack

Each pack contains everything a small model needs, so it never has to search:
- the exact deliverables (file, Java signature, rule line);
- the rules of the task;
- the decompiled 1.20.1 method;
- every 26.3 method with that name in the class hierarchy (found through the bytecode, since the method is often inherited);
- the mod's call sites (decompiled with readable names);
- an accepted example.

On a retry, the previous gate failure is included.

## Gates (run by the foundry, never by the agent)

1. **scope:** only the task's files changed. Editing or deleting tests and build files is rejected.
2. **build:** `./gradlew build` (everything compiles, unit tests pass).
3. **analyze:** the task's call now resolves (a shim with the wrong signature shows up as `RULE_BROKEN`), and no runtime finding exists that the baseline didn't have.
4. **gametests:** `./gradlew :boot:runGameTests` passes.

Landed work goes to `develop`, the integration branch. Review `develop` before merging it into `main`. Today the gates prove a shim is wired correctly. Per-rule behaviour tests (GameTests that exercise each shim) are the next step to also prove each shim does the right thing.

## Verified (with scripted agents in `foundry/test-agents/`)

| Agent | Result |
|---|---|
| correct `BlockState.is(Block)` shim | **landed** on `develop`: runtime findings 590 → 589 |
| shim with the wrong signature | rejected at **analyze** (`RULE_BROKEN`) |
| correct shim plus deleting a test | rejected at **scope** |
| wrong signature, five attempts | **escalated** with a ticket |

## Safety

- Agents work in a separate git worktree and branch. The main checkout is never touched.
- `corpus/` and `run/` are shared through links (Windows junctions). Cleanup deletes only the links, and refuses if a path is a real folder.
- `pi` has no sandbox of its own. For unattended runs, use a dedicated OS user, WSL2 or a container. The foundry limits what can *land*, not what an agent can do while it runs.
