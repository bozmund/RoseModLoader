# Rosetta and `rose analyze`

Rosetta is Rose's record of how old Minecraft APIs correspond to 26.3. `rose analyze` uses it to answer one question: **what exactly stands between this old mod jar and Minecraft 26.3?**

## Pipeline

```
corpus inputs (local)            Rosetta name layer (local)                 translate + analyze
-----------------------          --------------------------------          -------------------------------
MCPConfig 1.20.1 (SRG)    ─┐                                                mod.jar (Forge 1.20.1, SRG names)
Mojang 1.20.1 mappings    ─┤     corpus/rosetta/names-forge-1.20.1.tsv        │  RosettaRemapper (classes, SRG
intermediary 1.20.1       ─┼──▶  old runtime name → 26.3 name + "how"  ──▶    │  method/field families)
intermediary 1.21.11      ─┤                                                  ▼
Mojang 1.21.11 mappings   ─┤                                                every reference resolved against the
26.3 client jar           ─┤                                                26.3 jar + libraries (with inheritance)
rosetta/rules/*.tsv       ─┘                                                  ▼
                                                                           report: build/analyze/<jar>.rose.{md,json}
```

```bash
./gradlew corpusInputs          # mapping files, 1.21.11 mappings, pilot mod jars (rosetta/sources.json)
./gradlew :rosetta:buildNameLayers
./rose analyze corpus/mods/FarmersDelight-1.20.1-1.3.4.jar
```

## The name layer

The layer is built locally from Mojang's, Forge's and Fabric's mapping files and is never committed. Each entry records **how** it was resolved:

| How | Meaning |
|---|---|
| `SAME` | Same name, still in 26.3 |
| `INTERMEDIARY` | Renamed; followed through Fabric intermediary, which keeps one name per class and member from 1.14 to 1.21.11 |
| `NAME_MATCH` | Intermediary lost it, but 26.3 declares the same readable name with an identical translated signature in the same class |
| `RULE` | From a reviewed rule in `rosetta/rules/` (needs evidence) |
| `HEURISTIC` | A unique same-simple-name class appeared in 26.3, so it is probably a package move. Review it and promote it to a rule |
| `GONE` | No counterpart in 26.3: removed, split or merged. This needs semantic work |

SRG member names (`m_6227_`) identify a whole override family, so a mod's override is renamed together with the vanilla method it overrides, without needing the class hierarchy.

## Report statuses

| Status | Work | What to do |
|---|---|---|
| `CLASS_GONE`, `METHOD_GONE`, `FIELD_GONE` | era bridge | shim, redirect, or re-created class |
| `OVERRIDE_GONE` | era bridge | inheritance bridge (e.g. `Block.use` → `useItemOn`/`useWithoutItem`) |
| `SIGNATURE_CHANGED`, `OVERRIDE_SIGNATURE_CHANGED` | era bridge | adapter or redirect with argument conversion |
| `METHOD_MISSING`, `FIELD_MISSING` | era bridge | name maps but is gone in 26.x (after 1.21.11) |
| `FORGE_API`, `FORGE_EXTENSION` | dialect | Forge API to reimplement (`forgeSurface` in the JSON ranks it by use) |
| `UNKNOWN_CLASS` | dependency | another mod or library |
| `HEURISTIC_CLASS` | review | confirm the guessed package move |

Findings are split by **context**:
- **runtime:** blocks loading.
- **integration:** code in `integration`/`compat` packages that only runs with JEI, EMI and similar mods installed.
- **data generation:** build-time JSON generators.

Members whose signature mentions an unresolved class aren't listed again; fix the class first.

## First result: Farmer's Delight 1.20.1-1.3.4

289 classes, 34,681 references. Everything except the items below resolves automatically, including `ResourceLocation` → `Identifier` and package moves such as `advancements.critereon` → `advancements.triggers`.

| | Distinct |
|---|---:|
| Runtime problems | 591 |
| …of which era bridge (vanilla changed) | 489 |
| …of which Forge dialect | 102 |
| Optional integrations (JEI/EMI/CraftTweaker) | 65 |
| Data generators only | 208 |

Examples of real changes it found:
- `ItemStack.is(Item)` → `is(Predicate<Holder<Item>>)`;
- `InteractionResult.SUCCESS` changed type;
- `CompoundTag.getCompound` returns `Optional`;
- `Blocks.WHITE_WOOL` → `Blocks.WOOL` (a color collection);
- `GuiGraphics` replaced;
- `Block.use` split;
- `Minecraft.setScreen` → `minecraft.gui.setScreen`.

## Rules (committed, reviewed)

`rosetta/rules/*.tsv` holds Rose's own rules. Every rule needs an evidence column (a corpus file, changelog, porting primer or port-pair diff) and must be checked against `corpus/minecraft/26.3/src`. Semantic rules (redirects, inheritance bridges, adapters) are the next milestone's work. They will live next to these and be applied by the translation engine before Mixin.
