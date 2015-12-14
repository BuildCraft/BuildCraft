package buildcraft.core.blueprints.iterator;

import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlockBase;
import buildcraft.core.builders.BuildingSlotBlock;

/** A special building slot that applies post-processing to a block */
public class BuildingSlotPostProcess extends BuildingSlotBlock {
    public BuildingSlotPostProcess(SchematicBlockBase schematic, BlockPos pos) {
        this.schematic = schematic;
        this.pos = pos;
    }

    @Override
    public boolean writeToWorld(IBuilderContext context) {
        schematic.postProcessing(context, pos);
        return true;
    }

    @Override
    public List<ItemStack> getRequirements(IBuilderContext context) {
        return Collections.emptyList();
    }
}
