"""Generates era shims + redirect rules for 1.20.1 colored block/item fields that 26.x moved into ColorCollections.
Run from the repo root."""
import re
import zipfile

COLORS = ["WHITE", "ORANGE", "MAGENTA", "LIGHT_BLUE", "YELLOW", "LIME", "PINK", "GRAY", "LIGHT_GRAY", "CYAN",
          "PURPLE", "BLUE", "BROWN", "GREEN", "RED", "BLACK"]
ACCESSOR = {c: (c.lower().split("_")[0] + "".join(w.capitalize() for w in c.lower().split("_")[1:])) for c in COLORS}
# old suffix -> 26.3 collection name
SUFFIX = {"WOOL": "WOOL", "CARPET": "CARPET", "BED": "BED", "STAINED_GLASS": "STAINED_GLASS",
          "STAINED_GLASS_PANE": "STAINED_GLASS_PANE", "TERRACOTTA": "DYED_TERRACOTTA", "BANNER": "BANNER",
          "WALL_BANNER": "WALL_BANNER", "SHULKER_BOX": "DYED_SHULKER_BOX", "GLAZED_TERRACOTTA": "GLAZED_TERRACOTTA",
          "CONCRETE": "CONCRETE", "CONCRETE_POWDER": "CONCRETE_POWDER", "CANDLE": "DYED_CANDLE",
          "CANDLE_CAKE": "DYED_CANDLE_CAKE", "DYE": "DYE"}
CLASSES = {"net.minecraft.world.level.block.Blocks": ("Block", "net.minecraft.world.level.block.Block", "ColoredBlocks",
                                                       "corpus/minecraft/26.3/src/net/minecraft/world/level/block/Blocks.java"),
           "net.minecraft.world.item.Items": ("Item", "net.minecraft.world.item.Item", "ColoredItems",
                                              "corpus/minecraft/26.3/src/net/minecraft/world/item/Items.java")}

# Mojang 1.20.1: readable field -> official owner/name
mojang = {}
owner = None
for line in open("corpus/minecraft/1.20.1/client-mappings.txt", encoding="utf-8"):
    if not line.startswith(" "):
        m = re.match(r"(\S+) -> (\S+):", line)
        owner = (m.group(1), m.group(2)) if m else None
        continue
    if owner and owner[0] in CLASSES:
        m = re.match(r"\s+(\S+) (\w+) -> (\w+)$", line)
        if m:
            mojang[(owner[0], m.group(2))] = (owner[1], m.group(3))

# MCPConfig: official class/field -> SRG
srg = {}
tsrg = zipfile.ZipFile("corpus/mappings/mcp_config-1.20.1.zip").read("config/joined.tsrg").decode()
cls = None
for line in tsrg.splitlines():
    if not line.startswith("\t"):
        cls = line.split(" ")[0]
        continue
    parts = line.strip().split(" ")
    if len(parts) == 3 and cls and not parts[1].startswith("(") and parts[1].startswith("f_"):
        srg[(cls, parts[0])] = parts[1]

rules = []
for cname, (type_simple, type_fqn, shim, src26) in CLASSES.items():
    source26 = open(src26, encoding="utf-8").read()
    methods = []
    for color in COLORS:
        for suffix, collection in SUFFIX.items():
            field = f"{color}_{suffix}"
            if (cname, field) not in mojang:
                continue
            if re.search(rf"\b{type_simple} {field}\b", source26):
                continue  # still a plain field in 26.3
            if not re.search(rf"ColorCollection<{type_simple}> {collection} =", source26):
                continue
            official_owner, official_field = mojang[(cname, field)]
            srg_name = srg.get((official_owner, official_field))
            if not srg_name:
                continue
            methods.append(f"    public static {type_simple} {field}() {{\n        return {cname.split('.')[-1]}.{collection}.{ACCESSOR[color]}();\n    }}\n")
            rules.append((f"{cname.replace('.', '/')}.{srg_name}:L{type_fqn.replace('.', '/')};",
                          f"rose/era/v1_20_1/shim/{shim}.{field}",
                          f"26.x moved {cname.split('.')[-1]}.{field} into the ColorCollection {cname.split('.')[-1]}.{collection} ({ACCESSOR[color]}())"))
    java = (f"package rose.era.v1_20_1.shim;\n\nimport {type_fqn};\nimport {cname};\n\n"
            f"/** Redirect targets for 1.20.1 colored {type_simple.lower()} fields; 26.x keeps them in ColorCollections. Generated. */\n"
            f"public final class {shim} {{\n" + "\n".join(methods) + f"\n    private {shim}() {{}}\n}}\n")
    open(f"eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/{shim}.java", "w", newline="\n").write(java)
    print(shim, len(methods), "fields")

with open("rosetta/rules/forge-1.20.1/redirects.tsv", "a", newline="\n") as f:
    for r in rules:
        f.write("\t".join(r) + "\n")
print(len(rules), "rules")
