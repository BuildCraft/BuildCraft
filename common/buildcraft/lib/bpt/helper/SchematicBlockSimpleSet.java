package buildcraft.lib.bpt.helper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.bpt.IBuilderAccessor;
import buildcraft.api.bpt.IMaterialProvider;
import buildcraft.api.bpt.IMaterialProvider.IRequested;
import buildcraft.api.bpt.IMaterialProvider.IRequestedItem;
import buildcraft.api.bpt.SchematicBlock;
import buildcraft.api.bpt.SchematicException;
import buildcraft.api.mj.MjAPI;
import buildcraft.lib.bpt.task.TaskBuilder;
import buildcraft.lib.bpt.task.TaskUsable;
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
    public TaskUsable createTask(IBuilderAccessor builder, BlockPos pos) {
        IBlockState current = builder.getWorld().getBlockState(pos);
        if (current.equals(state)) {
            return TaskUsable.NOTHING;
        }
        TaskBuilder t = new TaskBuilder();
        IRequested req = t.request("state", state);
        t.doWhen(t.requirement().lock(req).power(2 * MjAPI.MJ), (b, p) -> {
            req.use();
            b.getWorld().setBlockState(p, state);
            SoundUtil.playBlockPlace(b.getWorld(), p);
        });
        return t.build().createUsableTask();
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
        IBlockState current = builder.getWorld().getBlockState(pos);
        if (current.equals(state)) {
            return DefaultBptActions.LEAVE;
        }
        return DefaultBptActions.REQUIRE_AIR;
    }
}
