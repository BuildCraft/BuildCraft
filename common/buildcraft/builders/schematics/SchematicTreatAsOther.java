package buildcraft.builders.schematics;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

/** This is designed for any blocks that shouln't be ignored, but should instead be treated as if they were another
 * block. A common example of this is all ore blocks- they are treated as if they were stone. */
public class SchematicTreatAsOther extends SchematicBlock {
    private final IBlockState otherState;

    public SchematicTreatAsOther(IBlockState state) {
        this.state = state;
        otherState = state;
    }

    @Override
    public void initializeFromObjectAt(IBuilderContext context, BlockPos pos) {
        this.state = otherState;
    }
}
