package buildcraft.lib.bpt.helper;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.bpt.IBptTask;
import buildcraft.api.bpt.IBuilderAccessor;
import buildcraft.api.bpt.Schematic.DefaultBptActions;
import buildcraft.api.bpt.Schematic.EnumPreBuildAction;
import buildcraft.api.bpt.Schematic.PreBuildAction;

/** Provides an implementation of {@link DefaultBptActions#REQUIRE_AIR} */
public enum VanillaBlockClearer implements PreBuildAction {
    INSTANCE;

    @Override
    public EnumPreBuildAction getType() {
        // Custom so it will not be replaced with itself
        return EnumPreBuildAction.CUSTOM_REMOVAL;
    }

    @Override
    public Collection<IBptTask> getTasks(IBuilderAccessor builder, BlockPos pos) {
        World world = builder.getWorld();
        if (world.isAirBlock(pos)) {
            return ImmutableList.of();
        }
        return ImmutableList.of(BptTaskBlockClear.create(world, pos));
    }

    @Override
    public int getTimeCost() {
        return 0;
    }
}
