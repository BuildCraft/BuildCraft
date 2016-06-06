package buildcraft.lib.bpt.helper;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.bpt.IBptTask;
import buildcraft.api.bpt.IBuilderAccessor;
import buildcraft.api.bpt.Schematic.PreBuildAction;
import buildcraft.api.bpt.Schematic.DefaultBptActions;
import buildcraft.api.bpt.Schematic.EnumPreBuildAction;
import buildcraft.lib.misc.PermissionUtil;
import buildcraft.lib.misc.PermissionUtil.PermissionBlock;

/** Provides an implementation of {@link DefaultBptActions#REQUIRE_AIR} */
public enum VanillaBlockClearer implements PreBuildAction {
    INSTANCE;

    @Override
    public EnumPreBuildAction getType() {
        // Custom so it will not be replaced with itself
        return EnumPreBuildAction.CUSTOM_REMOVAL;
    }

    @Override
    public Iterable<IBptTask> getTasks(IBuilderAccessor builder, BlockPos pos) {
        World world = builder.getWorld();
        if (world.isAirBlock(pos)) {
            return ImmutableList.of();
        }
        PermissionBlock target = PermissionUtil.createFrom(world, pos);
        if (PermissionUtil.hasPermission(PermissionUtil.PERM_DESTROY, builder.getOwner(), target)) {
            return ImmutableList.of(BptTaskBlockClear.create(world, pos));
        }
        return ImmutableList.of();
    }
}
