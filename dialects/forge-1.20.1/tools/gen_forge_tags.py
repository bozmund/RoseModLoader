"""Generates the dialect's forge: common tags (data/forge/tags/{item,block}) that Forge 1.20.1 shipped.
Entries are optional, so a missing id never fails a tag. Run from the repo root:
    python dialects/forge-1.20.1/tools/gen_forge_tags.py
"""
import json
import os

COLORS = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan",
          "purple", "blue", "brown", "green", "red", "black"]
MATERIALS = {"iron": "iron", "gold": "gold", "copper": "copper", "netherite": "netherite"}

items = {
    "crops/beetroot": ["minecraft:beetroot"], "crops/carrot": ["minecraft:carrot"],
    "crops/nether_wart": ["minecraft:nether_wart"], "crops/potato": ["minecraft:potato"],
    "crops/wheat": ["minecraft:wheat"],
    "crops": ["#forge:crops/beetroot", "#forge:crops/carrot", "#forge:crops/nether_wart", "#forge:crops/potato", "#forge:crops/wheat"],
    "seeds/beetroot": ["minecraft:beetroot_seeds"], "seeds/melon": ["minecraft:melon_seeds"],
    "seeds/pumpkin": ["minecraft:pumpkin_seeds"], "seeds/wheat": ["minecraft:wheat_seeds"],
    "seeds": ["#forge:seeds/beetroot", "#forge:seeds/melon", "#forge:seeds/pumpkin", "#forge:seeds/wheat"],
    "ingots/iron": ["minecraft:iron_ingot"], "ingots/gold": ["minecraft:gold_ingot"],
    "ingots/copper": ["minecraft:copper_ingot"], "ingots/netherite": ["minecraft:netherite_ingot"],
    "ingots/brick": ["minecraft:brick"], "ingots/nether_brick": ["minecraft:nether_brick"],
    "ingots": ["#forge:ingots/iron", "#forge:ingots/gold", "#forge:ingots/copper", "#forge:ingots/netherite",
               "#forge:ingots/brick", "#forge:ingots/nether_brick"],
    "nuggets/iron": ["minecraft:iron_nugget"], "nuggets/gold": ["minecraft:gold_nugget"],
    "nuggets": ["#forge:nuggets/iron", "#forge:nuggets/gold"],
    "gems/diamond": ["minecraft:diamond"], "gems/emerald": ["minecraft:emerald"], "gems/lapis": ["minecraft:lapis_lazuli"],
    "gems/quartz": ["minecraft:quartz"], "gems/amethyst": ["minecraft:amethyst_shard"],
    "gems/prismarine": ["minecraft:prismarine_crystals"],
    "gems": ["#forge:gems/diamond", "#forge:gems/emerald", "#forge:gems/lapis", "#forge:gems/quartz",
             "#forge:gems/amethyst", "#forge:gems/prismarine"],
    "dusts/redstone": ["minecraft:redstone"], "dusts/glowstone": ["minecraft:glowstone_dust"],
    "dusts": ["#forge:dusts/redstone", "#forge:dusts/glowstone"],
    "raw_materials/iron": ["minecraft:raw_iron"], "raw_materials/gold": ["minecraft:raw_gold"],
    "raw_materials/copper": ["minecraft:raw_copper"],
    "rods/wooden": ["minecraft:stick"], "rods/blaze": ["minecraft:blaze_rod"],
    "rods": ["#forge:rods/wooden", "#forge:rods/blaze"],
    "string": ["minecraft:string"], "leather": ["minecraft:leather"], "feathers": ["minecraft:feather"],
    "bones": ["minecraft:bone"], "eggs": ["minecraft:egg"], "slimeballs": ["minecraft:slime_ball"],
    "gunpowder": ["minecraft:gunpowder"], "ender_pearls": ["minecraft:ender_pearl"],
    "mushrooms": ["minecraft:brown_mushroom", "minecraft:red_mushroom"],
    "heads": ["minecraft:skeleton_skull", "minecraft:wither_skeleton_skull", "minecraft:zombie_head",
              "minecraft:creeper_head", "minecraft:player_head", "minecraft:dragon_head", "minecraft:piglin_head"],
    "stone": ["minecraft:stone", "minecraft:andesite", "minecraft:diorite", "minecraft:granite", "minecraft:deepslate", "minecraft:tuff"],
    "cobblestone": ["minecraft:cobblestone", "minecraft:mossy_cobblestone", "minecraft:cobbled_deepslate"],
    "sand": ["minecraft:sand", "minecraft:red_sand"], "gravel": ["minecraft:gravel"], "obsidian": ["minecraft:obsidian"],
    "netherrack": ["minecraft:netherrack"], "end_stones": ["minecraft:end_stone"],
    "glass": ["minecraft:glass", "minecraft:tinted_glass"], "glass_panes": ["minecraft:glass_pane"],
    "chests/wooden": ["minecraft:chest", "minecraft:trapped_chest"], "chests/ender": ["minecraft:ender_chest"],
    "chests/trapped": ["minecraft:trapped_chest"],
    "chests": ["#forge:chests/wooden", "#forge:chests/ender"],
    "barrels/wooden": ["minecraft:barrel"], "barrels": ["#forge:barrels/wooden"],
    "fences/wooden": ["#minecraft:wooden_fences"], "fences": ["#forge:fences/wooden", "minecraft:nether_brick_fence"],
    "fence_gates/wooden": ["#minecraft:fence_gates"], "fence_gates": ["#forge:fence_gates/wooden"],
    "shears": ["minecraft:shears"],
    "tools/axes": ["#minecraft:axes"], "tools/pickaxes": ["#minecraft:pickaxes"], "tools/shovels": ["#minecraft:shovels"],
    "tools/hoes": ["#minecraft:hoes"], "tools/swords": ["#minecraft:swords"], "tools/shields": ["minecraft:shield"],
    "tools/bows": ["minecraft:bow"], "tools/crossbows": ["minecraft:crossbow"], "tools/fishing_rods": ["minecraft:fishing_rod"],
    "tools/tridents": ["minecraft:trident"],
    "tools": ["#forge:tools/axes", "#forge:tools/pickaxes", "#forge:tools/shovels", "#forge:tools/hoes", "#forge:tools/swords"],
    "armors/helmets": ["#minecraft:head_armor"], "armors/chestplates": ["#minecraft:chest_armor"],
    "armors/leggings": ["#minecraft:leg_armor"], "armors/boots": ["#minecraft:foot_armor"],
    "armors": ["#forge:armors/helmets", "#forge:armors/chestplates", "#forge:armors/leggings", "#forge:armors/boots"],
}
for c in COLORS:
    items[f"dyes/{c}"] = [f"minecraft:{c}_dye"]
    items[f"glass/{c}"] = [f"minecraft:{c}_stained_glass"]
    items[f"glass_panes/{c}"] = [f"minecraft:{c}_stained_glass_pane"]
items["dyes"] = [f"#forge:dyes/{c}" for c in COLORS]
for m in ["iron", "gold", "copper", "coal", "diamond", "emerald", "lapis", "redstone", "quartz", "netherite_scrap"]:
    if m in ("quartz",):
        ores = ["minecraft:nether_quartz_ore"]
    elif m == "netherite_scrap":
        ores = ["minecraft:ancient_debris"]
    elif m in ("iron", "gold", "copper", "coal", "diamond", "emerald", "lapis", "redstone"):
        ores = [f"#minecraft:{m}_ores"]
    items[f"ores/{m}"] = ores
items["ores"] = [f"#forge:ores/{m}" for m in ["iron", "gold", "copper", "coal", "diamond", "emerald", "lapis", "redstone", "quartz", "netherite_scrap"]]
storage = {"iron": "iron_block", "gold": "gold_block", "copper": "copper_block", "netherite": "netherite_block",
           "diamond": "diamond_block", "emerald": "emerald_block", "lapis": "lapis_block", "redstone": "redstone_block",
           "coal": "coal_block", "raw_iron": "raw_iron_block", "raw_gold": "raw_gold_block", "raw_copper": "raw_copper_block",
           "amethyst": "amethyst_block", "quartz": "quartz_block", "wheat": "hay_block", "slime": "slime_block",
           "bone_meal": "bone_block", "dried_kelp": "dried_kelp_block"}
for k, v in storage.items():
    items[f"storage_blocks/{k}"] = [f"minecraft:{v}"]
items["storage_blocks"] = [f"#forge:storage_blocks/{k}" for k in storage]

# Block tags: the subset of item tags that name placeable blocks.
block_keys = [k for k in items if k.split("/")[0] in ("stone", "cobblestone", "sand", "gravel", "obsidian", "netherrack",
                                                      "end_stones", "glass", "glass_panes", "chests", "barrels", "fences",
                                                      "fence_gates", "ores", "storage_blocks", "mushrooms")]
blocks = {k: items[k] for k in block_keys}


def write(kind, tags):
    base = f"dialects/forge-1.20.1/src/main/resources/data/forge/tags/{kind}"
    for name, values in tags.items():
        entries = [{"id": v, "required": False} for v in values]
        path = os.path.join(base, name + ".json")
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w", newline="\n") as f:
            json.dump({"values": entries}, f, indent=2)
            f.write("\n")
    return len(tags)


print("item tags:", write("item", items), "block tags:", write("block", blocks))
