package buildcraft.lib.bpt.helper;

import com.google.common.collect.ImmutableList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.bpt.IBptTask;
import buildcraft.api.bpt.IBuilder;
import buildcraft.api.bpt.SchematicBlock;
import buildcraft.api.bpt.SchematicException;
import buildcraft.lib.misc.PermissionUtil;

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
    public Iterable<IBptTask> createTasks(IBuilder builder, BlockPos pos) {
        if (PermissionUtil.hasPermission(PermissionUtil.PERM_EDIT, builder.getOwner(), PermissionUtil.createFrom(builder.getWorld(), pos))) {
            return ImmutableList.of(new BptTaskBlockStandalone(pos, state));
        } else {
            return ImmutableList.of();
        }
    }

    @Override
    public PreBuildAction createClearingTask(IBuilder builder, BlockPos pos) {
        return DefaultBptActions.REQUIRE_AIR;
    }
}
