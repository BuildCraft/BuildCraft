package buildcraft.lib.bpt.vanilla;

import com.google.common.collect.ImmutableList;

import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.bpt.IBptTask;
import buildcraft.api.bpt.IBuilderAccessor;
import buildcraft.api.bpt.SchematicBlock;
import buildcraft.api.bpt.SchematicFactoryWorldBlock;

public class SchematicAir extends SchematicBlock {
    public static final SchematicAir INSTANCE = new SchematicAir();
    public static final SchematicFactoryWorldBlock FACTORY = (world, pos) -> INSTANCE;

    private SchematicAir() {
        super(Blocks.AIR);
    }

    @Override
    public Iterable<IBptTask> createTasks(IBuilderAccessor builder, BlockPos pos) {
        return ImmutableList.of();
    }

    @Override
    public PreBuildAction createClearingTask(IBuilderAccessor builder, BlockPos pos) {
        return DefaultBptActions.REQUIRE_AIR;
    }
}
