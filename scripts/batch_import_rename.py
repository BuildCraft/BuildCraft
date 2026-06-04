#!/usr/bin/env python3
"""
Batch import + body symbol rename for BuildCraft Forge→Fabric migration.
Pass 1: import-only replacements
Pass 2: body-level symbol renames (unambiguous type names only)
Pass 3: @SideOnly annotation replacements
"""

import os
import re
import sys
import argparse
from pathlib import Path

# -- Import line replacements (exact old → new package) --
IMPORT_REPLACEMENTS = [
    ("net.minecraft.nbt.NBTTagCompound",     "net.minecraft.nbt.NbtCompound"),
    ("net.minecraft.nbt.NBTTagList",          "net.minecraft.nbt.NbtList"),
    ("net.minecraft.nbt.NBTTagString",        "net.minecraft.nbt.NbtString"),
    ("net.minecraft.nbt.NBTTagInt",           "net.minecraft.nbt.NbtInt"),
    ("net.minecraft.nbt.NBTTagFloat",         "net.minecraft.nbt.NbtFloat"),
    ("net.minecraft.nbt.NBTTagDouble",        "net.minecraft.nbt.NbtDouble"),
    ("net.minecraft.nbt.NBTTagByte",          "net.minecraft.nbt.NbtByte"),
    ("net.minecraft.nbt.NBTTagLong",          "net.minecraft.nbt.NbtLong"),
    ("net.minecraft.nbt.NBTTagByteArray",     "net.minecraft.nbt.NbtByteArray"),
    ("net.minecraft.nbt.NBTTagIntArray",      "net.minecraft.nbt.NbtIntArray"),
    ("net.minecraft.util.EnumFacing",         "net.minecraft.util.math.Direction"),
    ("net.minecraft.entity.player.EntityPlayer",   "net.minecraft.entity.player.PlayerEntity"),
    ("net.minecraft.entity.player.EntityPlayerMP", "net.minecraft.server.network.ServerPlayerEntity"),
    ("net.minecraft.block.state.IBlockState", "net.minecraft.block.BlockState"),
    ("net.minecraft.util.ResourceLocation",   "net.minecraft.util.Identifier"),
    ("net.minecraft.util.EnumHand",           "net.minecraft.util.Hand"),
    ("net.minecraft.util.EnumActionResult",   "net.minecraft.util.ActionResult"),
    ("net.minecraft.tileentity.TileEntity",   "net.minecraft.block.entity.BlockEntity"),
    ("net.minecraft.entity.item.EntityItem",  "net.minecraft.entity.ItemEntity"),
    ("net.minecraft.util.math.AxisAlignedBB", "net.minecraft.util.math.Box"),
    ("net.minecraft.util.text.TextFormatting","net.minecraft.util.Formatting"),
    ("net.minecraft.network.PacketBuffer",    "net.minecraft.network.PacketByteBuf"),
    ("net.minecraftforge.fml.relauncher.SideOnly", "net.fabricmc.api.Environment"),
    ("net.minecraftforge.fml.relauncher.Side",     "net.fabricmc.api.EnvType"),
]

# -- Body-level symbol renames: (old_symbol, new_symbol)
# Only unambiguous type names — not method names.
BODY_REPLACEMENTS = [
    ("NBTTagCompound",  "NbtCompound"),
    ("NBTTagList",      "NbtList"),
    ("NBTTagString",    "NbtString"),
    ("NBTTagInt",       "NbtInt"),
    ("NBTTagFloat",     "NbtFloat"),
    ("NBTTagDouble",    "NbtDouble"),
    ("NBTTagByte",      "NbtByte"),
    ("NBTTagLong",      "NbtLong"),
    ("NBTTagByteArray", "NbtByteArray"),
    ("NBTTagIntArray",  "NbtIntArray"),
    ("EnumFacing",      "Direction"),
    ("EntityPlayer",    "PlayerEntity"),
    ("EntityPlayerMP",  "ServerPlayerEntity"),
    ("IBlockState",     "BlockState"),
    ("ResourceLocation","Identifier"),
    ("EnumHand",        "Hand"),
    ("EnumActionResult","ActionResult"),
    ("TileEntity",      "BlockEntity"),
    ("EntityItem",      "ItemEntity"),
    ("AxisAlignedBB",   "Box"),
    ("TextFormatting",  "Formatting"),
    ("PacketBuffer",    "PacketByteBuf"),
]

# -- @SideOnly annotation replacements --
SIDEONLY_REPLACEMENTS = [
    (r'@SideOnly\(Side\.CLIENT\)', '@Environment(EnvType.CLIENT)'),
    (r'@SideOnly\(Side\.SERVER\)', '@Environment(EnvType.SERVER)'),
]

# Files/directories to skip (already migrated libLeaf files live here)
SKIP_DIRS = set()  # Currently not skipping any — libLeaf files already have correct imports


def is_import_line(line: str) -> bool:
    stripped = line.strip()
    return stripped.startswith("import ")


def replace_imports(lines: list[str]) -> tuple[list[str], int]:
    """Replace import lines only."""
    changes = 0
    result = []
    for line in lines:
        if is_import_line(line):
            new_line = line
            for old, new in IMPORT_REPLACEMENTS:
                # Match the full import path (with optional semicolon/whitespace)
                if old in new_line:
                    new_line = new_line.replace(old, new)
            if new_line != line:
                changes += 1
            result.append(new_line)
        else:
            result.append(line)
    return result, changes


def replace_body_symbols(lines: list[str]) -> tuple[list[str], int]:
    """Replace type symbols in non-import lines using word-boundary matching."""
    changes = 0
    result = []
    for line in lines:
        if is_import_line(line):
            result.append(line)
            continue
        new_line = line
        for old, new in BODY_REPLACEMENTS:
            # Word boundary match — avoids replacing substrings of other identifiers
            pattern = r'\b' + re.escape(old) + r'\b'
            replaced = re.sub(pattern, new, new_line)
            if replaced != new_line:
                changes += 1
                new_line = replaced
        result.append(new_line)
    return result, changes


def replace_sideonly(lines: list[str]) -> tuple[list[str], int]:
    """Replace @SideOnly annotations in all lines."""
    changes = 0
    result = []
    for line in lines:
        new_line = line
        for pattern, replacement in SIDEONLY_REPLACEMENTS:
            replaced = re.sub(pattern, replacement, new_line)
            if replaced != new_line:
                changes += 1
                new_line = replaced
        result.append(new_line)
    return result, changes


def process_file(path: Path, dry_run: bool = False) -> dict:
    try:
        original = path.read_text(encoding="utf-8", errors="replace")
    except Exception as e:
        return {"path": str(path), "error": str(e)}

    lines = original.splitlines(keepends=True)

    lines, import_changes = replace_imports(lines)
    lines, body_changes = replace_body_symbols(lines)
    lines, sideonly_changes = replace_sideonly(lines)

    total = import_changes + body_changes + sideonly_changes
    if total == 0:
        return {"path": str(path), "import_changes": 0, "body_changes": 0, "sideonly_changes": 0}

    if not dry_run:
        path.write_text("".join(lines), encoding="utf-8")

    return {
        "path": str(path),
        "import_changes": import_changes,
        "body_changes": body_changes,
        "sideonly_changes": sideonly_changes,
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("root", help="Root directory to scan (e.g. common/)")
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--sample", type=int, default=0, help="Only process N files (for test run)")
    args = parser.parse_args()

    root = Path(args.root)
    java_files = sorted(root.rglob("*.java"))

    if args.sample:
        java_files = java_files[:args.sample]

    print(f"Scanning {len(java_files)} Java files under {root}...")

    modified = []
    errors = []
    total_imports = 0
    total_body = 0
    total_sideonly = 0

    for f in java_files:
        result = process_file(f, dry_run=args.dry_run)
        if "error" in result:
            errors.append(result)
        elif result.get("import_changes", 0) + result.get("body_changes", 0) + result.get("sideonly_changes", 0) > 0:
            modified.append(result)
            total_imports += result["import_changes"]
            total_body += result["body_changes"]
            total_sideonly += result["sideonly_changes"]

    print(f"\n{'DRY RUN — no files written' if args.dry_run else 'Files written.'}")
    print(f"Modified files: {len(modified)}")
    print(f"  Import line changes: {total_imports}")
    print(f"  Body symbol changes: {total_body}")
    print(f"  @SideOnly changes:   {total_sideonly}")
    if errors:
        print(f"  Errors: {len(errors)}")
        for e in errors[:5]:
            print(f"    {e}")

    if args.sample and modified:
        print("\nSample file diffs (first 5 modified):")
        for r in modified[:5]:
            print(f"  {r['path']}  imports={r['import_changes']} body={r['body_changes']} sideonly={r['sideonly_changes']}")

    return 0


if __name__ == "__main__":
    sys.exit(main())
