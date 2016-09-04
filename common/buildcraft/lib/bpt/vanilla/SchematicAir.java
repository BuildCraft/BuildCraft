package buildcraft.lib.bpt.vanilla;

import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.bpt.*;
import buildcraft.lib.bpt.task.TaskUsable;

public class SchematicAir extends SchematicBlock {
    public static final SchematicAir INSTANCE = new SchematicAir();
    public static final SchematicFactoryWorldBlock FACTORY_WORLD = (world, pos) -> INSTANCE;
    public static final SchematicFactoryNBTBlock FACTORY_NBT = (nbt) -> INSTANCE;

    private SchematicAir() {
        super(Blocks.AIR);
    }

    @Override
    public TaskUsable createTask(IBuilderAccessor builder, BlockPos pos) {
        return TaskUsable.NOTHING;
    }

    @Override
    public PreBuildAction createClearingTask(IBuilderAccessor builder, BlockPos pos) {
        return DefaultBptActions.REQUIRE_AIR;
    }

    @Override
    public boolean buildImmediatly(World world, IMaterialProvider provider, BlockPos pos) {
        world.setBlockToAir(pos);
        return true;
    }

    @Override
    public int getTimeCost() {
        return 1;
    }
}
