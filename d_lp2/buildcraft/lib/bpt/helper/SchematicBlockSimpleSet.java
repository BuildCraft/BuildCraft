package buildcraft.lib.bpt.helper;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.bpt.IBptTask;
import buildcraft.api.bpt.IBuilder;
import buildcraft.api.bpt.SchematicBlock;
import buildcraft.api.bpt.SchematicException;

/** Designates a block that either exists fully or not: like wooden planks or iron bars- the block takes up the entire
 * block ID and only has differing state associated with it. It also does not require any other blocks around it to
 * work. */
public class SchematicBlockSimpleSet extends SchematicBlock {
    public SchematicBlockSimpleSet(IBlockState state) {
        super(state);
    }

    public SchematicBlockSimpleSet(NBTTagCompound nbt, BlockPos offset) throws SchematicException {
        super(nbt, offset);
    }

    @Override
    public Iterable<IBptTask> createTasks(IBuilder builder) {
        // TODO: Some sort of advanced clearing.

        return ImmutableSet.of(BptTaskBlockClear.create(builder, offset), BptTaskBlockStandalone.create(builder, offset, state));
    }
}
