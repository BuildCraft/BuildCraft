package buildcraft.lib.bpt.helper;

import com.google.common.collect.ImmutableList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.bpt.*;
import buildcraft.api.bpt.IMaterialProvider.IRequestedItem;
import buildcraft.lib.misc.SoundUtil;

/** Designates a block that either exists fully or not: like wooden planks or iron bars- the block takes up the entire
 * block ID and only has differing state associated with it. It also does not require any other blocks around it to
 * work. */
public class SchematicBlockSimpleSet extends SchematicBlock {
    public SchematicBlockSimpleSet(World world, BlockPos pos) {
        super(world, pos);
    }

    public SchematicBlockSimpleSet(NBTTagCompound nbt) throws SchematicException {
        super(nbt);
    }

    @Override
    public Iterable<IBptTask> createTasks(IBuilderAccessor builder, BlockPos pos) {
        return ImmutableList.of(new BptTaskBlockStandalone(pos, state, builder));
    }

    @Override
    public boolean buildImmediatly(World world, IMaterialProvider provider, BlockPos pos) {
        IRequestedItem req = provider.requestStackForBlock(state);
        if (req.lock()) {
            req.use();
            world.setBlockState(pos, state);
            SoundUtil.playBlockPlace(world, pos, state);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public PreBuildAction createClearingTask(IBuilderAccessor builder, BlockPos pos) {
        return DefaultBptActions.REQUIRE_AIR;
    }
}
