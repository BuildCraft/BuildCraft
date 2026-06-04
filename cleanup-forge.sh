#!/usr/bin/env bash
# =============================================================================
# cleanup-forge.sh
# =============================================================================
# Removes or archives Forge-specific files that are no longer needed after the
# Fabric Loom migration.
#
# REVIEW THIS SCRIPT CAREFULLY BEFORE RUNNING.
# All destructive actions are commented out by default.  Uncomment a block
# only after you are satisfied that it is safe to proceed.
#
# Usage:
#   bash cleanup-forge.sh            # dry-run: only prints what would happen
#   CONFIRM=yes bash cleanup-forge.sh  # execute the deletions
# =============================================================================

set -euo pipefail

DRY_RUN=true
if [[ "${CONFIRM:-}" == "yes" ]]; then
    DRY_RUN=false
fi

do_rm() {
    local target="$1"
    if [[ -e "$target" ]]; then
        if $DRY_RUN; then
            echo "[DRY RUN] would remove: $target"
        else
            echo "Removing: $target"
            rm -rf "$target"
        fi
    else
        echo "[SKIP] not found: $target"
    fi
}

do_rename() {
    local src="$1" dst="$2"
    if [[ -e "$src" ]]; then
        if $DRY_RUN; then
            echo "[DRY RUN] would rename: $src → $dst"
        else
            echo "Renaming: $src → $dst"
            mv "$src" "$dst"
        fi
    else
        echo "[SKIP] not found: $src"
    fi
}

echo "======================================================================"
echo "  BuildCraft Forge → Fabric cleanup script"
if $DRY_RUN; then
    echo "  MODE: DRY RUN  (set CONFIRM=yes to apply changes)"
fi
echo "======================================================================"
echo ""

# ------------------------------------------------------------------------------
# 1. Forge build properties — replaced by gradle/libs.versions.toml
# ------------------------------------------------------------------------------
echo "--- Section 1: Forge build metadata ---"
do_rm "build.properties"
do_rm "private.properties.example"   # SSH deploy config — not used in Fabric build

# ------------------------------------------------------------------------------
# 2. Forge mod descriptor — replaced by src/main/resources/fabric.mod.json
# ------------------------------------------------------------------------------
echo ""
echo "--- Section 2: FML mod descriptor ---"
do_rm "buildcraft_resources/mcmod.info"

# ------------------------------------------------------------------------------
# 3. Old Forge resource artifacts inside buildcraft_resources
#    (pack.mcmeta / pack.png belong to resource packs, not Fabric mods)
# ------------------------------------------------------------------------------
echo ""
echo "--- Section 3: Resource-pack metadata (not needed for Fabric mods) ---"
do_rm "buildcraft_resources/pack.mcmeta"
do_rm "buildcraft_resources/pack.png"

# ------------------------------------------------------------------------------
# 4. @Mod entry-point Java files
#    These are LISTED only — do not delete them.  They must be converted to
#    ModInitializer / ClientModInitializer classes in a later Java porting phase.
#    See migration-plan.md Phase 2 for the conversion procedure.
# ------------------------------------------------------------------------------
echo ""
echo "--- Section 4: @Mod annotated files (LIST ONLY — do NOT delete) ---"
echo "  The following files contain @Mod annotations and must be rewritten as"
echo "  Fabric ModInitializer / ClientModInitializer entrypoints:"
echo ""
FORGE_MOD_FILES=(
    "common/buildcraft/lib/BCLib.java"
    "common/buildcraft/core/BCCore.java"
    "common/buildcraft/energy/BCEnergy.java"
    "common/buildcraft/energy/BCEnergyWorldGen.java"
    "common/buildcraft/factory/BCFactory.java"
    "common/buildcraft/robotics/BCRobotics.java"
    "common/buildcraft/silicon/BCSilicon.java"
    "common/buildcraft/silicon/BCSiliconRecipes.java"
    "common/buildcraft/transport/BCTransport.java"
    "common/buildcraft/transport/BCTransportRecipes.java"
    "common/buildcraft/builders/BCBuilders.java"
)
for f in "${FORGE_MOD_FILES[@]}"; do
    if [[ -f "$f" ]]; then
        echo "    [KEEP, REWRITE LATER] $f"
    else
        echo "    [NOT FOUND] $f"
    fi
done

# ------------------------------------------------------------------------------
# 5. @Mod.EventBusSubscriber / @SubscribeEvent companion files
#    Also LISTED only.  These need their event subscriptions ported to
#    Fabric API event hooks (see migration-plan.md, Section 3g).
# ------------------------------------------------------------------------------
echo ""
echo "--- Section 5: EventBusSubscriber files (LIST ONLY — do NOT delete) ---"
echo "  The following files use @SubscribeEvent and must be ported to Fabric API events:"
echo ""
EVENT_FILES=(
    "common/buildcraft/lib/BCLibEventDist.java"
    "common/buildcraft/core/BCCoreEventDist.java"
    "common/buildcraft/energy/BCEnergyWorldGen.java"
    "common/buildcraft/factory/BCFactoryEventDist.java"
    "common/buildcraft/transport/BCTransportEventDist.java"
    "common/buildcraft/builders/BCBuildersEventDist.java"
    "common/buildcraft/robotics/BCRobotics.java"
)
for f in "${EVENT_FILES[@]}"; do
    if [[ -f "$f" ]]; then
        echo "    [KEEP, REWRITE LATER] $f"
    else
        echo "    [NOT FOUND] $f"
    fi
done

# ------------------------------------------------------------------------------
# 6. Forge config system files
#    net.minecraftforge.common.config.Configuration is gone in Fabric.
#    These BCxxxConfig.java files must be rewritten using a Fabric config library
#    (e.g., Cloth Config, or vanilla Options).  LISTED only for now.
# ------------------------------------------------------------------------------
echo ""
echo "--- Section 6: Forge config classes (LIST ONLY — do NOT delete) ---"
echo "  Rewrite using a Fabric-compatible config library (Cloth Config recommended):"
echo ""
CONFIG_FILES=$(find common/buildcraft -name "*Config.java" 2>/dev/null | sort)
for f in $CONFIG_FILES; do
    echo "    [KEEP, REWRITE LATER] $f"
done

# ------------------------------------------------------------------------------
# 7. Forge proxy files (BCxxxProxy.java)
#    SidedProxy is gone in Fabric.  Proxy classes must be replaced with
#    ModInitializer (server) + ClientModInitializer (client) pattern.
# ------------------------------------------------------------------------------
echo ""
echo "--- Section 7: Forge SidedProxy classes (LIST ONLY — do NOT delete) ---"
PROXY_FILES=$(find common/buildcraft -name "*Proxy.java" 2>/dev/null | sort)
for f in $PROXY_FILES; do
    echo "    [KEEP, REWRITE LATER] $f"
done

# ------------------------------------------------------------------------------
# 8. Old Forge build artifacts / IDE files that can be safely archived
# ------------------------------------------------------------------------------
echo ""
echo "--- Section 8: Forge build artifacts / legacy IDE project files ---"
do_rm ".gradle"           # Gradle 4.3.1 cache — incompatible with Gradle 8
do_rm "run"               # Old Forge run directory (stale; runClient will recreate)

# .classpath and .project are Eclipse project files generated by ForgeGradle.
# If you use Eclipse: re-run './gradlew eclipse' after migration (if still needed).
do_rm ".classpath"
do_rm ".project"
do_rm ".settings"

# ------------------------------------------------------------------------------
# 9. Old changelog / versions.txt — optional housekeeping
#    These are kept in buildcraft_resources/changelog/ and are harmless.
#    Archive them only if you want a clean resource directory.
# ------------------------------------------------------------------------------
echo ""
echo "--- Section 9: Legacy changelog / version files (OPTIONAL, not removed) ---"
echo "  buildcraft_resources/changelog/  (${CHLOG_COUNT:-$(ls buildcraft_resources/changelog/ 2>/dev/null | wc -l | tr -d ' ')} files)"
echo "  buildcraft_resources/versions.txt"
echo "  → These are informational only; no action taken."

# ------------------------------------------------------------------------------
# 10. src_old_license (was already commented out in old sourceSets)
# ------------------------------------------------------------------------------
echo ""
echo "--- Section 10: src_old_license directory ---"
do_rm "src_old_license"

echo ""
echo "======================================================================"
if $DRY_RUN; then
    echo "  Dry run complete.  No files were modified."
    echo "  Re-run with CONFIRM=yes to apply changes."
else
    echo "  Cleanup complete."
fi
echo "======================================================================"
