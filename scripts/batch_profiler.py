#!/usr/bin/env python3
"""Pass 5 — profiler API + GlStateManager → RenderSystem method renames."""
import re, sys, argparse
from pathlib import Path

REPLACEMENTS = [
    # Profiler field access and method renames
    (".mcProfiler.",     ".getProfiler()."),
    (".mcProfiler",      ".getProfiler()"),   # standalone access
    (".startSection(",   ".push("),
    (".endSection()",    ".pop()"),
    (".endStartSection(",".swap("),
    # GlStateManager → RenderSystem (common ones)
    ("GlStateManager.color(",            "RenderSystem.setShaderColor("),
    ("GlStateManager.enableBlend()",     "RenderSystem.enableBlend()"),
    ("GlStateManager.disableBlend()",    "RenderSystem.disableBlend()"),
    ("GlStateManager.enableDepth()",     "RenderSystem.enableDepthTest()"),
    ("GlStateManager.disableDepth()",    "RenderSystem.disableDepthTest()"),
    ("GlStateManager.enableTexture2D()", ""),   # no-op in 1.20
    ("GlStateManager.disableTexture2D()",""),
    ("GlStateManager.enableLighting()",  ""),
    ("GlStateManager.disableLighting()", ""),
    ("GlStateManager.enableAlpha()",     ""),
    ("GlStateManager.disableAlpha()",    ""),
    ("GlStateManager.pushMatrix()",      "RenderSystem.getModelViewStack().push()"),
    ("GlStateManager.popMatrix()",       "RenderSystem.getModelViewStack().pop()"),
    ("GlStateManager.translate(",        "RenderSystem.getModelViewStack().translate("),
    ("GlStateManager.scale(",            "RenderSystem.getModelViewStack().scale("),
    ("GlStateManager.rotate(",           "RenderSystem.getModelViewStack().rotate("),
    ("GlStateManager.resetColor()",      "RenderSystem.setShaderColor(1f, 1f, 1f, 1f)"),
    # BufferBuilder/Tessellator API
    (".pos(",            ".vertex("),
    (".tex(",            ".texture("),
    (".normal(",         ".normal("),     # same
    (".endVertex()",     ".next()"),
    ("Tessellator.getInstance().getBuffer()", "Tessellator.getInstance().getBuffer()"),  # same
    (".draw()",          ".draw()"),      # same
    # World block entity
    (".getWorld().getTileEntity(", ".getWorld().getBlockEntity("),
]

def is_import_line(line):
    return line.strip().startswith("import ")

def process_file(path, dry_run=False):
    try:
        original = path.read_text(encoding="utf-8", errors="replace")
    except Exception as e:
        return {"path": str(path), "error": str(e)}
    lines = original.splitlines(keepends=True)
    result = []
    total = 0
    for line in lines:
        if is_import_line(line):
            result.append(line)
            continue
        new_line = line
        for old, new in REPLACEMENTS:
            if old and old != new and old in new_line:
                new_line = new_line.replace(old, new)
                total += 1
        result.append(new_line)
    if total == 0:
        return {"path": str(path), "changes": 0}
    if not dry_run:
        path.write_text("".join(result), encoding="utf-8")
    return {"path": str(path), "changes": total}

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("roots", nargs="+")
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()
    java_files = []
    for root in args.roots:
        java_files.extend(sorted(Path(root).rglob("*.java")))
    print(f"Scanning {len(java_files)} files...")
    modified = []
    total = 0
    for f in java_files:
        r = process_file(f, args.dry_run)
        if r.get("changes", 0) > 0:
            modified.append(r)
            total += r["changes"]
    print(f"{'DRY RUN' if args.dry_run else 'Written'}.")
    print(f"Modified: {len(modified)} files | substitutions: {total}")

if __name__ == "__main__":
    main()
