package buildcraft.lib.bpt.vanilla;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.bpt.IBptTask;
import buildcraft.api.bpt.IBuilderAccessor;
import buildcraft.api.bpt.SchematicBlock;

public class SchematicChest extends SchematicBlock {
    private final ItemStack[] stacks = new ItemStack[27];

    public SchematicChest(IBlockState at, TileEntityChest chest) {
        super(at);
        for (int i = 0; i < 27; i++) {
            stacks[i] = chest.getStackInSlot(i);
        }
    }

    @Override
    public Iterable<IBptTask> createTasks(IBuilderAccessor builder, BlockPos pos) {
        if (canEdit(builder, pos)) {
            return ImmutableList.of(new BptTaskPlaceAndFillChest(pos, state, stacks, builder));
        } else {
            return ImmutableList.of();
        }
    }

    @Override
    public PreBuildAction createClearingTask(IBuilderAccessor builder, BlockPos pos) {
        return DefaultBptActions.REQUIRE_AIR;
    }
}
