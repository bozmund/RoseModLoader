#!/usr/bin/env python3
"""Downloads the newest Forge 1.20.1 build of Modrinth projects into corpus/mods/<dir>/ (git-ignored).

Usage: fetch_modrinth.py <dir> <slug>...   Prints each file with its license; checks the SHA-1 Modrinth lists.
"""
import hashlib
import json
import os
import sys
import urllib.parse
import urllib.request

API = "https://api.modrinth.com/v2"
HEADERS = {"User-Agent": "rose-mod-loader-corpus/0.1"}


def get(url):
    with urllib.request.urlopen(urllib.request.Request(url, headers=HEADERS), timeout=60) as response:
        return response.read()


def main(target, slugs):
    out = os.path.join("corpus", "mods", target)
    os.makedirs(out, exist_ok=True)
    for slug in slugs:
        project = json.loads(get(f"{API}/project/{slug}"))
        query = urllib.parse.urlencode({"loaders": '["forge"]', "game_versions": '["1.20.1"]'})
        versions = json.loads(get(f"{API}/project/{slug}/version?{query}"))
        if not versions:
            print(f"{slug}: no Forge 1.20.1 build")
            continue
        files = [f for f in versions[0]["files"] if f["primary"]] or versions[0]["files"]
        file = files[0]
        path = os.path.join(out, file["filename"])
        if not os.path.exists(path):
            data = get(file["url"])
            if hashlib.sha1(data).hexdigest() != file["hashes"]["sha1"]:
                raise SystemExit(f"{slug}: SHA-1 mismatch for {file['filename']}")
            with open(path, "wb") as f:
                f.write(data)
        print(f"{slug}\t{project['license']['id']}\t{path}")


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2:])
