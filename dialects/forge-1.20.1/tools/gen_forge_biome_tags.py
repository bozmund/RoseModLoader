"""Generates the dialect's forge: biome tags (data/forge/tags/worldgen/biome) from Forge 1.20.1's own data.
Source: corpus/forge/forge-1.20.1-47.3.0-universal.jar (git-ignored; download it from maven.minecraftforge.net).
Entries are made optional, so a biome missing on 26.3 never fails a tag. Run from the repo root:
    python dialects/forge-1.20.1/tools/gen_forge_biome_tags.py
"""
import json
import os
import zipfile

JAR = "corpus/forge/forge-1.20.1-47.3.0-universal.jar"
PREFIX = "data/forge/tags/worldgen/biome/"
OUT = "dialects/forge-1.20.1/src/main/resources/data/forge/tags/worldgen/biome"

with zipfile.ZipFile(JAR) as z:
    count = 0
    for name in z.namelist():
        if not name.startswith(PREFIX) or not name.endswith(".json"):
            continue
        tag = json.loads(z.read(name))
        tag["values"] = [{"id": v, "required": False} if isinstance(v, str) else {**v, "required": False} for v in tag["values"]]
        path = os.path.join(OUT, name[len(PREFIX):])
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w", newline="\n") as f:
            json.dump(tag, f, indent=2)
            f.write("\n")
        count += 1
print(f"wrote {count} biome tags")
