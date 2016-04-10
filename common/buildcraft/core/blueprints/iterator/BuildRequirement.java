package buildcraft.core.blueprints.iterator;

import net.minecraft.util.math.BlockPos;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlockBase;
import buildcraft.builders.schematics.SchematicAir;
import buildcraft.core.builders.BuildingSlotBlock;
import buildcraft.core.builders.BuildingSlotBlock.Mode;

public class BuildRequirement {
    /** The actual requirements for this. */
    private BuildingSlotBlock slot;
    private boolean hasBuilt, hasPostProcessed;

    /** @param schematic The schematic to take from
     * @param context The context to build in
     * @param pos The world position to update for */
    public void updateFor(SchematicBlockBase schematic, IBuilderContext context, BlockPos pos) {
        if (schematic.isAlreadyBuilt(context, pos)) return;

        if (schematic instanceof SchematicAir) {
            slot = new BuildingSlotBlock();
            slot.mode = Mode.ClearIfInvalid;
            slot.pos = pos;
            slot.schematic = schematic;
        } else {
            slot = new BuildingSlotBlock();
            slot.mode = Mode.Build;
            slot.pos = pos;
            slot.schematic = schematic;
        }
    }

    public boolean hasBuilt() {
        return hasBuilt;
    }

    public boolean hasCompletlyFinished() {
        return hasBuilt && hasPostProcessed;
    }

    public BuildingSlotBlock getSlot() {
        return slot;
    }

    public BuildingSlotPostProcess forPostProcess() {
        if (slot == null) return null;
        if (slot instanceof BuildingSlotPostProcess) return (BuildingSlotPostProcess) slot;
        BuildingSlotPostProcess post = new BuildingSlotPostProcess(slot.getSchematic(), slot.pos);
        slot = post;
        return post;
    }

    public void tick() {
        if (slot.built) {
            if (slot.mode == Mode.Build) {
                if (!hasBuilt) {
                    hasBuilt = true;
                    slot = forPostProcess();
                } else hasPostProcessed = true;
            } else {
                slot = null;
                hasPostProcessed = true;
            }
        }
    }
}
