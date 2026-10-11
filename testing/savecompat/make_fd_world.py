#!/usr/bin/env python3
"""Turns a vanilla 1.20.1 flat world into a Farmer's Delight 1.20.1 save, for the save compatibility check.

FD 1.20.1 blocks and block entities are written into chunk (0, 0) the way Forge 1.20.1 saved them: palette entries
with FD ids and properties, block entities with FD's 1.20.1 NBT (item stacks as {id, Count, tag}). Rose then opens
the world (DataFixerUpper upgrades the vanilla parts; FD's own data is read by FD's 1.20.1 load code).

Usage: make_fd_world.py <world dir>   (needs nbtlib; the world comes from a vanilla 1.20.1 server, level-type flat)
"""
import io
import math
import struct
import sys
import zlib

from nbtlib import Byte, Compound, File, Int, List, LongArray, String

Y = -60  # the first air layer of a default flat world

# x -> (block state, block entity or None); all at y=Y, z=2, in chunk (0, 0)
BLOCKS = {
    2: (("farmersdelight:cooking_pot", {"facing": "north", "support": "none", "waterlogged": "false"}), Compound({
        "id": String("farmersdelight:cooking_pot"),
        "Inventory": Compound({"Size": Int(9), "Items": List[Compound]([
            Compound({"Slot": Int(0), "id": String("minecraft:carrot"), "Count": Byte(3)}),
            Compound({"Slot": Int(6), "id": String("farmersdelight:beef_stew"), "Count": Byte(2)}),
        ])}),
        "CookTime": Int(0), "CookTimeTotal": Int(200),
        "Container": Compound({"id": String("minecraft:bowl"), "Count": Byte(1)}),
    })),
    4: (("farmersdelight:cutting_board", {"facing": "north", "waterlogged": "false"}), Compound({
        "id": String("farmersdelight:cutting_board"),
        "Inventory": Compound({"Size": Int(1), "Items": List[Compound]([
            Compound({"Slot": Int(0), "id": String("farmersdelight:cabbage"), "Count": Byte(1)}),
        ])}),
        "IsItemCarved": Byte(0),
    })),
    # the id before FD renamed it to bamboo_basket (FD keeps a registry alias)
    6: (("farmersdelight:basket", {"facing": "up", "waterlogged": "false"}), Compound({
        "id": String("farmersdelight:basket"),
        "Items": List[Compound]([Compound({"Slot": Byte(0), "id": String("farmersdelight:tomato"), "Count": Byte(5)})]),
        "TransferCooldown": Int(-1),
    })),
    8: (("farmersdelight:canvas_sign", {"rotation": "0", "waterlogged": "false"}), Compound({
        "id": String("farmersdelight:canvas_sign"),
        "front_text": Compound({"messages": List[String]([String('"Rose"'), String('"loads"'), String('"FD"'), String('""')]),
                                "color": String("black"), "has_glowing_text": Byte(0)}),
        "back_text": Compound({"messages": List[String]([String('""')] * 4), "color": String("black"), "has_glowing_text": Byte(0)}),
        "is_waxed": Byte(0),
    })),
    10: (("minecraft:chest", {"facing": "north", "type": "single", "waterlogged": "false"}), Compound({
        "id": String("minecraft:chest"),
        "Items": List[Compound]([
            Compound({"Slot": Byte(0), "id": String("farmersdelight:skillet"), "Count": Byte(1), "tag": Compound({"Damage": Int(7)})}),
            Compound({"Slot": Byte(1), "id": String("farmersdelight:hot_cocoa"), "Count": Byte(4)}),
            Compound({"Slot": Byte(2), "id": String("farmersdelight:flint_knife"), "Count": Byte(1),
                      "tag": Compound({"display": Compound({"Name": String('{"text":"Old knife"}')})})}),
        ]),
    })),
    12: (("farmersdelight:rich_soil", {}), None),
}


def read_chunk(region, cx, cz):
    index = 4 * ((cx & 31) + (cz & 31) * 32)
    offset = int.from_bytes(region[index:index + 3], "big") * 4096
    length, compression = struct.unpack(">IB", region[offset:offset + 5])
    assert compression == 2, "zlib chunks only"
    return File.parse(io.BytesIO(zlib.decompress(region[offset + 5:offset + 4 + length])))


def write_chunk(region, cx, cz, chunk):
    out = io.BytesIO()
    chunk.write(out)
    data = zlib.compress(out.getvalue())
    payload = struct.pack(">IB", len(data) + 1, 2) + data
    sectors = math.ceil(len(payload) / 4096)
    payload += b"\0" * (sectors * 4096 - len(payload))
    region = bytearray(region)
    start = len(region) // 4096  # appended: the old copy stays as dead space
    region += payload
    index = 4 * ((cx & 31) + (cz & 31) * 32)
    region[index:index + 4] = start.to_bytes(3, "big") + bytes([sectors])
    return bytes(region)


def unpack(data, bits):
    per_long = 64 // bits
    mask = (1 << bits) - 1
    values = []
    for long in data:
        long &= (1 << 64) - 1
        for i in range(per_long):
            values.append((long >> (i * bits)) & mask)
    return values[:4096]


def pack(values, bits):
    per_long = 64 // bits
    longs = []
    for start in range(0, 4096, per_long):
        long = 0
        for i, value in enumerate(values[start:start + per_long]):
            long |= value << (i * bits)
        longs.append(long - (1 << 64) if long >= 1 << 63 else long)
    return LongArray(longs)


def state(name, properties):
    entry = Compound({"Name": String(name)})
    if properties:
        entry["Properties"] = Compound({k: String(v) for k, v in properties.items()})
    return entry


def main(world):
    path = f"{world}/region/r.0.0.mca"
    with open(path, "rb") as f:
        region = f.read()
    chunk = read_chunk(region, 0, 0)
    root = chunk  # 1.18+ chunks have no "Level" wrapper
    section = next(s for s in root["sections"] if s["Y"] == Y >> 4)
    states = section["block_states"]
    palette = list(states["palette"])
    bits = max(4, math.ceil(math.log2(len(palette)))) if "data" in states else 0
    values = unpack(states["data"], bits) if bits else [0] * 4096
    for x, ((name, properties), _) in BLOCKS.items():
        entry = state(name, properties)
        if entry not in palette:
            palette.append(entry)
        values[((Y & 15) * 16 + 2) * 16 + x] = palette.index(entry)
    bits = max(4, math.ceil(math.log2(len(palette))))
    states["palette"] = List[Compound](palette)
    states["data"] = pack(values, bits)
    entities = list(root.get("block_entities", []))
    for x, (_, entity) in BLOCKS.items():
        if entity is not None:
            entity.update({"x": Int(x), "y": Int(Y), "z": Int(2), "keepPacked": Byte(0)})
            entities.append(entity)
    root["block_entities"] = List[Compound](entities)
    with open(path, "wb") as f:
        f.write(write_chunk(region, 0, 0, chunk))
    print(f"wrote {len(BLOCKS)} Farmer's Delight 1.20.1 blocks into {path}")


if __name__ == "__main__":
    main(sys.argv[1])
