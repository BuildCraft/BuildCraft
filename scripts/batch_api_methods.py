#!/usr/bin/env python3
"""
Pass 4 — method call renames for NBT API + world API changes.
All replacements are method invocations, not symbol names — use string replace only.
"""

import re
import sys
import argparse
from pathlib import Path

# Method call substitutions: (old, new)
# Only rename where the mapping is unambiguous.
METHOD_REPLACEMENTS = [
    # NbtCompound (was NBTTagCompound) method renames
    (".hasKey(",          ".contains("),
    (".getTag(",          ".get("),
    (".getInteger(",      ".getInt("),
    (".getLong(",         ".getLong("),     # same
    (".getFloat(",        ".getFloat("),    # same
    (".getDouble(",       ".getDouble("),   # same
    (".getString(",       ".getString("),   # same
    (".getBoolean(",      ".getBoolean("),  # same
    (".getByte(",         ".getByte("),     # same
    (".getCompoundTag(",  ".getCompound("),
    (".getTagList(",      ".getList("),
    (".setTag(",          ".put("),
    (".setInteger(",      ".putInt("),
    (".setLong(",         ".putLong("),
    (".setFloat(",        ".putFloat("),
    (".setDouble(",       ".putDouble("),
    (".setString(",       ".putString("),
    (".setBoolean(",      ".putBoolean("),
    (".setByte(",         ".putByte("),
    (".setIntArray(",     ".putIntArray("),
    (".setByteArray(",    ".putByteArray("),
    # World API
    (".getTileEntity(",   ".getBlockEntity("),
    (".isAirBlock(",      ".isAir("),
    # Entity
    (".getHeldItem(",     ".getStackInHand("),
    (".getHeldItemMainhand(", ".getMainHandStack("),
    (".getHeldItemOffhand(",  ".getOffHandStack("),
    # BlockPos / world
    (".notifyBlockUpdate(", ".updateListeners("),
    (".markDirty()",        ".markDirty()"),  # same
]

# Exact string replacements in body (not import lines)
EXACT_REPLACEMENTS = [
    # World.isRemote already done in pass 3 but catch stragglers
    ("world.isRemote",    "world.isClient"),
    ("getWorld().isRemote", "getWorld().isClient"),
    ("getEntityWorld().isRemote", "getEntityWorld().isClient"),
    # Profiler access
    ("world.theProfiler", "world.getProfiler()"),
    ("world.mcProfiler",  "world.getProfiler()"),
    # Constants.NBT type IDs (Forge) → NbtElement type IDs (Yarn)
    ("Constants.NBT.TAG_COMPOUND",  "NbtElement.COMPOUND_TYPE"),
    ("Constants.NBT.TAG_LIST",      "NbtElement.LIST_TYPE"),
    ("Constants.NBT.TAG_STRING",    "NbtElement.STRING_TYPE"),
    ("Constants.NBT.TAG_INT",       "NbtElement.INT_TYPE"),
    ("Constants.NBT.TAG_BYTE",      "NbtElement.BYTE_TYPE"),
    ("Constants.NBT.TAG_LONG",      "NbtElement.LONG_TYPE"),
    ("Constants.NBT.TAG_FLOAT",     "NbtElement.FLOAT_TYPE"),
    ("Constants.NBT.TAG_DOUBLE",    "NbtElement.DOUBLE_TYPE"),
    ("Constants.NBT.TAG_BYTE_ARRAY","NbtElement.BYTE_ARRAY_TYPE"),
    ("Constants.NBT.TAG_INT_ARRAY", "NbtElement.INT_ARRAY_TYPE"),
    ("Constants.NBT.TAG_SHORT",     "NbtElement.SHORT_TYPE"),
]


def is_import_line(line: str) -> bool:
    return line.strip().startswith("import ")


def process_line(line: str) -> tuple[str, int]:
    if is_import_line(line):
        return line, 0

    new_line = line
    changes = 0

    for old, new in EXACT_REPLACEMENTS:
        if old != new and old in new_line:
            new_line = new_line.replace(old, new)
            changes += 1

    for old, new in METHOD_REPLACEMENTS:
        if old != new and old in new_line:
            new_line = new_line.replace(old, new)
            changes += 1

    return new_line, changes


def process_file(path, dry_run=False):
    try:
        original = path.read_text(encoding="utf-8", errors="replace")
    except Exception as e:
        return {"path": str(path), "error": str(e)}

    lines = original.splitlines(keepends=True)
    result = []
    total = 0
    for line in lines:
        new_line, n = process_line(line)
        result.append(new_line)
        total += n

    if total == 0:
        return {"path": str(path), "changes": 0}

    if not dry_run:
        path.write_text("".join(result), encoding="utf-8")

    return {"path": str(path), "changes": total}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("roots", nargs="+")
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--sample", type=int, default=0)
    args = parser.parse_args()

    java_files = []
    for root in args.roots:
        java_files.extend(sorted(Path(root).rglob("*.java")))

    if args.sample:
        java_files = java_files[:args.sample]

    print(f"Scanning {len(java_files)} files...")
    modified = []
    total_changes = 0

    for f in java_files:
        r = process_file(f, args.dry_run)
        if r.get("changes", 0) > 0:
            modified.append(r)
            total_changes += r["changes"]

    print(f"{'DRY RUN' if args.dry_run else 'Written'}.")
    print(f"Modified: {len(modified)} files | total substitutions: {total_changes}")
    if args.sample:
        for r in modified[:10]:
            print(f"  {r['path']}  changes={r['changes']}")


if __name__ == "__main__":
    main()
