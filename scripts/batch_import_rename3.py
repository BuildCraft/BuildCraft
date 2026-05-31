#!/usr/bin/env python3
"""
Pass 3 — body-level renames for remaining high-frequency symbols.
"""

import re
import sys
import argparse
from pathlib import Path

IMPORT_REPLACEMENTS = [
    ("net.minecraft.world.WorldServer",               "net.minecraft.server.world.ServerWorld"),
    ("net.minecraft.client.renderer.RenderHelper",    "net.minecraft.client.render.RenderHelper"),
    ("net.minecraft.util.ITooltipFlag",               "net.minecraft.client.item.TooltipContext"),
    ("net.minecraft.util.EnumBlockRenderType",        "net.minecraft.block.BlockRenderType"),
    ("net.minecraft.util.BlockRenderLayer",           "net.minecraft.client.render.RenderLayer"),
    ("net.minecraft.block.BlockRenderLayer",          "net.minecraft.client.render.RenderLayer"),
    ("net.minecraft.entity.EntityLiving",             "net.minecraft.entity.mob.MobEntity"),
    ("net.minecraft.entity.player.EntityPlayer",      "net.minecraft.entity.player.PlayerEntity"),
    ("net.minecraft.block.state.IBlockState",         "net.minecraft.block.BlockState"),
]

BODY_REPLACEMENTS = [
    # Side enum values used in bodies (not @SideOnly annotations)
    ("Side.SERVER",           "EnvType.SERVER"),
    ("Side.CLIENT",           "EnvType.CLIENT"),
    # World.isRemote → World.isClient
    (".isRemote",             ".isClient"),
    # WorldServer → ServerWorld
    ("WorldServer",           "ServerWorld"),
    # Tooltip
    ("ITooltipFlag",          "TooltipContext"),
    # Block render
    ("BlockRenderLayer",      "RenderLayer"),
    ("EnumBlockRenderType",   "BlockRenderType"),
    # Entity
    ("EntityLiving",          "MobEntity"),
    # Rotation (should be BlockRotation from pass 2 but verify)
    # NBTTagCompound stragglers
    ("NBTTagCompound",        "NbtCompound"),
    ("NBTTagList",            "NbtList"),
    ("NBTTagString",          "NbtString"),
    ("NBTBase",               "NbtElement"),
    # ResourceLocation stragglers
    ("ResourceLocation",      "Identifier"),
    # EnumFacing stragglers
    ("EnumFacing",            "Direction"),
    # EntityPlayer stragglers
    ("EntityPlayer",          "PlayerEntity"),
    # IBlockState stragglers
    ("IBlockState",           "BlockState"),
    # NonNullList stragglers
    ("NonNullList",           "DefaultedList"),
    # EnumDyeColor stragglers
    ("EnumDyeColor",          "DyeColor"),
]


def is_import_line(line: str) -> bool:
    return line.strip().startswith("import ")


def replace_imports(lines):
    changes = 0
    result = []
    for line in lines:
        if is_import_line(line):
            new_line = line
            for old, new in IMPORT_REPLACEMENTS:
                if old in new_line:
                    new_line = new_line.replace(old, new)
            if new_line != line:
                changes += 1
            result.append(new_line)
        else:
            result.append(line)
    return result, changes


def replace_body_symbols(lines):
    changes = 0
    result = []
    for line in lines:
        if is_import_line(line):
            result.append(line)
            continue
        new_line = line
        for old, new in BODY_REPLACEMENTS:
            if old == new:
                continue
            if old.startswith("."):
                # Field/method access pattern — match `.isRemote` etc.
                if old in new_line:
                    new_line = new_line.replace(old, new)
                    changes += 1
            elif "." in old and not old.startswith("("):
                # Qualified name like Side.SERVER
                if old in new_line:
                    new_line = new_line.replace(old, new)
                    changes += 1
            else:
                pattern = r'\b' + re.escape(old) + r'\b'
                replaced = re.sub(pattern, new, new_line)
                if replaced != new_line:
                    changes += 1
                    new_line = replaced
        result.append(new_line)
    return result, changes


def process_file(path, dry_run=False):
    try:
        original = path.read_text(encoding="utf-8", errors="replace")
    except Exception as e:
        return {"path": str(path), "error": str(e)}

    lines = original.splitlines(keepends=True)
    lines, import_changes = replace_imports(lines)
    lines, body_changes = replace_body_symbols(lines)

    total = import_changes + body_changes
    if total == 0:
        return {"path": str(path), "import_changes": 0, "body_changes": 0}

    if not dry_run:
        path.write_text("".join(lines), encoding="utf-8")

    return {"path": str(path), "import_changes": import_changes, "body_changes": body_changes}


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
    total_imports = total_body = 0

    for f in java_files:
        r = process_file(f, args.dry_run)
        if r.get("import_changes", 0) + r.get("body_changes", 0) > 0:
            modified.append(r)
            total_imports += r["import_changes"]
            total_body += r["body_changes"]

    print(f"{'DRY RUN' if args.dry_run else 'Written'}.")
    print(f"Modified: {len(modified)} files | imports: {total_imports} | body: {total_body}")
    if args.sample:
        for r in modified[:10]:
            print(f"  {r['path']}  i={r['import_changes']} b={r['body_changes']}")


if __name__ == "__main__":
    main()
