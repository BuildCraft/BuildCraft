package buildcraft.api.bpt.helper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.BCBlocks;
import buildcraft.api.bpt.IBptTask;
import buildcraft.api.bpt.IBuilder;
import buildcraft.api.mj.helpers.task.MjTaskOnce;

public class BptTaskBlockClear extends MjTaskOnce implements IBptTask {
    private static final ItemStack stack = new ItemStack(Item.getItemFromBlock(BCBlocks.coreDecorated));
    private final int ticks;
    private final IBuilder builder;
    private final BlockPos pos;

    public static BptTaskBlockClear create(IBuilder builder, BlockPos offset) {
        BlockPos pos = builder.getPos().add(offset);
        World world = builder.getWorld();
        IBlockState state = world.getBlockState(pos);
        int milliJoules = (int) (state.getBlockHardness(world, pos) * 1000);
        int ticks = milliJoules / 200;
        return new BptTaskBlockClear(milliJoules, ticks, builder, pos);
    }

    private BptTaskBlockClear(int milliJoules, int ticks, IBuilder builder, BlockPos pos) {
        super(milliJoules, ticks, true);
        this.ticks = ticks;
        this.builder = builder;
        this.pos = pos;
    }

    @Override
    public boolean isDone() {
        return builder.getWorld().isAirBlock(pos);
    }

    @Override
    public boolean isReadyYet() {
        return true;
    }

    @Override
    protected void onRecievePower(int mJSoFar) {
        for (int i = 0; i < ticks; i += 2)
            builder.startBlockBuilding(pos, stack, i);
        builder.addAction(ticks, new ActionSetBlockState());
    }
}
