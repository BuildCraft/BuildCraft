package buildcraft.builders.schematics;

import net.minecraft.util.BlockPos;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;

public class SchematicBrewingStand extends SchematicTile {
    @Override
    public void initializeFromObjectAt(IBuilderContext context, BlockPos pos) {
        super.initializeFromObjectAt(context, pos);

        if (tileNBT != null) {
            tileNBT.removeTag("BrewTime");
        }
    }
}
