#!/usr/bin/env python3
"""
Pass 2 — additional import + body symbol renames not covered in pass 1.
"""

import os
import re
import sys
import argparse
from pathlib import Path

IMPORT_REPLACEMENTS = [
    # Client
    ("net.minecraft.client.Minecraft",                    "net.minecraft.client.MinecraftClient"),
    ("net.minecraft.client.renderer.GlStateManager",      "com.mojang.blaze3d.systems.RenderSystem"),
    ("net.minecraft.client.renderer.RenderHelper",        "net.minecraft.client.render.RenderHelper"),
    ("net.minecraft.client.renderer.Tessellator",         "net.minecraft.client.render.Tessellator"),
    ("net.minecraft.client.renderer.BufferBuilder",       "net.minecraft.client.render.BufferBuilder"),
    ("net.minecraft.client.renderer.vertex.DefaultVertexFormats", "net.minecraft.client.render.VertexFormats"),
    ("net.minecraft.client.renderer.entity.RenderManager","net.minecraft.client.render.entity.EntityRenderDispatcher"),
    ("net.minecraft.client.renderer.texture.TextureAtlasSprite", "net.minecraft.client.texture.Sprite"),
    ("net.minecraft.client.renderer.texture.TextureMap",  "net.minecraft.client.texture.SpriteAtlasTexture"),
    ("net.minecraft.client.renderer.block.model.ModelResourceLocation", "net.minecraft.client.util.ModelIdentifier"),
    ("net.minecraft.client.renderer.block.model.IBakedModel", "net.minecraft.client.render.model.BakedModel"),
    ("net.minecraft.client.resources.IResourceManager",   "net.minecraft.resource.ResourceManager"),
    ("net.minecraft.client.gui.FontRenderer",             "net.minecraft.client.font.TextRenderer"),
    # NBT
    ("net.minecraft.nbt.NBTBase",                         "net.minecraft.nbt.NbtElement"),
    ("net.minecraft.nbt.NBTTagShort",                     "net.minecraft.nbt.NbtShort"),
    ("net.minecraft.nbt.NBTTagEnd",                       "net.minecraft.nbt.NbtEnd"),
    # World / block
    ("net.minecraft.world.IBlockAccess",                  "net.minecraft.world.BlockView"),
    ("net.minecraft.world.World",                         "net.minecraft.world.World"),  # no-op verify
    ("net.minecraft.block.material.Material",             "net.minecraft.block.Material"),
    ("net.minecraft.block.properties.IProperty",          "net.minecraft.state.property.Property"),
    ("net.minecraft.block.properties.PropertyBool",       "net.minecraft.state.property.BooleanProperty"),
    ("net.minecraft.block.properties.PropertyInteger",    "net.minecraft.state.property.IntProperty"),
    ("net.minecraft.block.properties.PropertyEnum",       "net.minecraft.state.property.EnumProperty"),
    ("net.minecraft.block.state.BlockStateContainer",     "net.minecraft.state.StateManager"),
    # Items / crafting
    ("net.minecraft.util.NonNullList",                    "net.minecraft.util.collection.DefaultedList"),
    ("net.minecraft.item.crafting.IRecipe",               "net.minecraft.recipe.Recipe"),
    ("net.minecraft.item.crafting.Ingredient",            "net.minecraft.recipe.Ingredient"),
    ("net.minecraft.item.crafting.ShapedRecipes",         "net.minecraft.recipe.ShapedRecipe"),
    ("net.minecraft.item.crafting.ShapelessRecipes",      "net.minecraft.recipe.ShapelessRecipe"),
    ("net.minecraft.item.EnumDyeColor",                   "net.minecraft.util.DyeColor"),
    ("net.minecraft.creativetab.CreativeTabs",            "net.minecraft.item.ItemGroup"),
    # Entities
    ("net.minecraft.entity.EntityLivingBase",             "net.minecraft.entity.LivingEntity"),
    ("net.minecraft.entity.Entity",                       "net.minecraft.entity.Entity"),  # verify
    ("net.minecraft.entity.player.InventoryPlayer",       "net.minecraft.entity.player.PlayerInventory"),
    # Profiler
    ("net.minecraft.profiler.Profiler",                   "net.minecraft.util.profiler.Profiler"),
    # Text/chat
    ("net.minecraft.util.text.ITextComponent",            "net.minecraft.text.Text"),
    ("net.minecraft.util.text.TextComponentString",       "net.minecraft.text.LiteralText"),
    ("net.minecraft.util.text.TextComponentTranslation",  "net.minecraft.text.TranslatableText"),
    ("net.minecraft.util.text.Style",                     "net.minecraft.text.Style"),
    # init packages (replaced by Registries in 1.20)
    ("net.minecraft.init.Blocks",                         "net.minecraft.block.Blocks"),
    ("net.minecraft.init.Items",                          "net.minecraft.item.Items"),
    # Sound
    ("net.minecraft.util.SoundCategory",                  "net.minecraft.sound.SoundCategory"),
    ("net.minecraft.util.SoundEvent",                     "net.minecraft.sound.SoundEvent"),
    # Math / misc
    ("net.minecraft.util.math.RayTraceResult",            "net.minecraft.util.hit.HitResult"),
    ("net.minecraft.util.math.Vec3d",                     "net.minecraft.util.math.Vec3d"),  # same in yarn
    ("net.minecraft.util.Mirror",                         "net.minecraft.util.BlockMirror"),
    ("net.minecraft.util.Rotation",                       "net.minecraft.util.BlockRotation"),
    # ICommand
    ("net.minecraft.command.ICommandSender",              "net.minecraft.server.command.ServerCommandSource"),
    ("net.minecraft.command.CommandBase",                 "com.mojang.brigadier.Command"),
]

BODY_REPLACEMENTS = [
    # Additional symbols from pass 1 gaps
    ("NBTBase",           "NbtElement"),
    ("NBTTagShort",       "NbtShort"),
    ("NonNullList",       "DefaultedList"),
    ("IBlockAccess",      "BlockView"),
    ("EntityLivingBase",  "LivingEntity"),
    ("EnumDyeColor",      "DyeColor"),
    ("ITextComponent",    "Text"),
    ("TextComponentString", "LiteralText"),
    ("TextComponentTranslation", "TranslatableText"),
    ("IProperty",         "Property"),
    ("PropertyBool",      "BooleanProperty"),
    ("PropertyInteger",   "IntProperty"),
    ("PropertyEnum",      "EnumProperty"),
    ("CreativeTabs",      "ItemGroup"),
    ("InventoryPlayer",   "PlayerInventory"),
    ("RayTraceResult",    "HitResult"),
    ("FontRenderer",      "TextRenderer"),
    ("TextureAtlasSprite","Sprite"),
    ("ModelResourceLocation", "ModelIdentifier"),
    ("IBakedModel",       "BakedModel"),
    ("IResourceManager",  "ResourceManager"),
    ("BufferBuilder",     "BufferBuilder"),  # same name, different package
    ("Tessellator",       "Tessellator"),   # same name
    # Minecraft client singleton rename
    ("Minecraft.getMinecraft()", "MinecraftClient.getInstance()"),
    ("Minecraft",         "MinecraftClient"),
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
                if old != new and old in new_line:
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
            # Special case: method call replacement (no word boundary needed)
            if "()" in old:
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
    parser.add_argument("root")
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--sample", type=int, default=0)
    args = parser.parse_args()

    root = Path(args.root)
    java_files = sorted(root.rglob("*.java"))
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
