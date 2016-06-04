package buildcraft.lib.bpt.vanilla;

import com.google.common.collect.ImmutableList;

import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.bpt.IBptTask;
import buildcraft.api.bpt.IBuilder;
import buildcraft.api.bpt.SchematicBlock;

public class SchematicAir extends SchematicBlock {
    public SchematicAir() {
        super(Blocks.AIR);
    }

    @Override
    public Iterable<IBptTask> createTasks(IBuilder builder, BlockPos pos) {
        return ImmutableList.of();
    }

    @Override
    public PreBuildAction createClearingTask(IBuilder builder, BlockPos pos) {
        return DefaultBptActions.REQUIRE_AIR;
    }
}
