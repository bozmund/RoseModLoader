# Rose Agent Bridge

The Agent Bridge lets AI agents (and scripts) **launch, observe, control and test** a running Rose game. Anything a player does through the window, a model can do through structured calls.

```
  agent (Claude, pi + local model, scripts)
     │  MCP (rose mcp)   or   shell (rose ctl ...)
     ▼
  bridge-cli  ──HTTP JSON-RPC, 127.0.0.1 + token──▶  rose_bridge mod inside the game
                                                       ├─ server methods (dedicated / integrated / GameTest server)
                                                       ├─ client methods (screens, clicks, screenshots, the local player)
                                                       └─ event log (log lines, chat, action bar, joins)
```

## Quick start

```bash
./rose launch client                    # Windows: rose.cmd launch client — waits until the bridge answers
./rose ctl client.createTestWorld       # creative flat test world, waits until the player is in it
./rose ctl client.player                # where am I?
./rose ctl world.setBlock x=10 y=4 z=-6 block=sample:counter_block
./rose ctl client.useBlock x=10 y=4 z=-6 face=west
./rose events --types overlay           # {"type":"overlay","data":{"text":"Counter: 1"}}
./rose ctl client.screenshot            # returns the PNG path
./rose stop client
./rose test "sample:*"                  # GameTests, headless: JSON pass/fail per test, exit 1 on failure
```

`rose ctl` takes `key=value` pairs. Values are read as JSON where possible (numbers, booleans, arrays), otherwise as strings. Use `--params '{...}'` for anything complex and `--target client|server|gametest` to pick a game when several run. All output is JSON.

## MCP (Claude Code and other MCP clients)

The repo's `.mcp.json` registers the server `rose` (`rose.cmd mcp`). Its tools:

| Tool | Does |
|---|---|
| `rose_launch` | Start `client` or `server` and wait for the bridge |
| `rose_status` | Which targets are running |
| `rose_methods` | The running game's method list (self-describing) |
| `rose_call` | Call any method: `{method, params, target?}` |
| `rose_events` | Poll events after a sequence number |
| `rose_screenshot` | Returns the client window as an **image** |
| `rose_stop` | Quit a game and wait until it has exited |
| `rose_test` | Run GameTests headless, pass/fail per test |

## Methods

Call `rose.methods` for the live list. Each method lists its parameters and a description.

**Every side**

| Method | Params | Returns / does |
|---|---|---|
| `rose.ping` | | side, Minecraft version, game dir, whether a server runs |
| `rose.methods` | | all methods |
| `rose.mods` | | loaded mods |
| `events.poll` | `after?`, `types?`, `limit?` | `{events, lastSeq}`. Types: `log`, `chat`, `overlay`, `player_join`, `server_started`, `server_stopping` |
| `registry.list` | `registry`, `namespace?` | ids, e.g. every `sample:` block |

**Server** (dedicated, GameTest, or the client's integrated server once a world is open)

| Method | Params | Returns / does |
|---|---|---|
| `server.status` | | running, tick count, levels, players |
| `server.command` | `command` | runs it as the console (full permissions) and returns the feedback lines |
| `server.players` | | name, dimension, position, health, game mode, bot? |
| `world.getBlock` | `x y z dimension?` | block, state, block entity SNBT |
| `world.setBlock` | `x y z block dimension?` | `block` accepts states: `minecraft:oak_stairs[facing=east]` |
| `player.useBlock` | `player x y z face? hand?` | a server-side player (real or bot) right-clicks a block |
| `bot.spawn` | `name x? y? z? dimension?` | joins a bot player through the real join path |
| `bot.list` / `bot.remove` | `name` | |
| `bot.payloads` | `name clear?` | mod packets the server sent this bot (proves server→client networking) |
| `server.reload` | | reloads data packs (recipes, loot, tags, test instances) |
| `server.stop` | | stops the server |

**Client**

| Method | Params | Returns / does |
|---|---|---|
| `client.status` | | screen, in world?, fps, window minimized/focused, position |
| `client.screen` | | open screen and its widgets (index, type, text, active, bounds) |
| `client.click` | `index` or `text` | clicks a widget (a real left click) |
| `client.setText` | `index`/`text`, `value` | fills a text field |
| `client.screenshot` | | PNG path |
| `client.createTestWorld` | | new creative flat test world, enters it |
| `client.openWorld` | `name` | opens a saved world |
| `client.connect` | `address` | joins a multiplayer server |
| `client.disconnect` | | back to the title screen |
| `client.player` | | position, rotation, health, food, held item, inventory |
| `client.useBlock` | `x y z face?` | a real right-click: client → network → server, including reach checks |
| `client.getBlock` | `x y z` | the block and its block entity data as the **client** has them; compare with `world.getBlock` to see whether data reached the client (sync bugs show up here) |
| `client.key` | `name action?` | drives a key mapping (`key.use`, `key.attack`, ...): `click` (default) queues one press, `press`/`release` hold it; holding `key.use` keeps using the held item |
| `client.chat` | `message` | chat, or a command when it starts with `/` |
| `client.quit` | | closes the client |

## Things worth knowing (learned the hard way)

- **First launch shows an accessibility welcome screen.** An agent dismisses it with `client.click text=Continue`. It only appears until it has been dismissed once.
- **Reach matters.** `client.useBlock` on a block more than ~4.5 blocks away returns a client-side `Success`, but the server ignores it, as for a real player. Place test blocks next to `client.player`'s position.
- **Mouse buttons follow SDL3 in 26.3:** left = 1. The bridge handles this; old mods may not (see the primer).
- `client.createTestWorld` confirms vanilla's "experimental settings" warning by itself. That warning comes from the test world's custom flat dimensions, not from mods.
- The dev tasks copy mod jars into `run/<side>/.rose-dev-mods/` at launch, so rebuilding while a game runs is safe. The running game keeps its old copy.

## Safety

- The bridge only exists in dev runs (`-Drose.bridge=true`, which the Gradle run tasks set). Normal play has no listener.
- It binds to `127.0.0.1` only. Every call needs the per-launch bearer token from `run/<side>/rose-bridge.json`, which is deleted on exit.
- `server.command` runs with full permissions. The token and the localhost-only binding are what keep that safe.

## Not built yet

- Crash reports with a "Rose translation context" section. This needs the translation engine (M3).
- Hot reload of Rosetta rules and patches (M3+). `server.reload` covers data packs today.
- Walking and looking (`client.move`).

## Multiplayer runs

A real client can join a dedicated server: `./rose launch server` (accept Mojang's EULA in `run/server/eula.txt` first), then `./rose ctl --target server server.command command="whitelist off"` (26.3 servers start with the whitelist on), `./rose launch client` and `./rose ctl --target client client.connect address=127.0.0.1:25565`. Pass `--target` on every call while both run.
