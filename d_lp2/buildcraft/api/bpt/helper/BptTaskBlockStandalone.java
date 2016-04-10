package buildcraft.api.bpt.helper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.bpt.IBptTask;
import buildcraft.api.bpt.IBuilder;
import buildcraft.api.mj.helpers.task.MjTaskOnce;

public class BptTaskBlockStandalone extends MjTaskOnce implements IBptTask {
    private final IBuilder builder;
    private final BlockPos pos;
    private final IBlockState state;
    private final ItemStack display;

    public static BptTaskBlockStandalone create(IBuilder builder, BlockPos offset, IBlockState state) {
        Item item = Item.getItemFromBlock(state.getBlock());
        if (item == null) throw new IllegalArgumentException("Cannot find an item for " + state);
        ItemStack stack = new ItemStack(item, state.getBlock().getMetaFromState(state));
        return new BptTaskBlockStandalone(builder, offset, state, stack);
    }

    public BptTaskBlockStandalone(IBuilder builder, BlockPos offset, IBlockState state, ItemStack display) {
        super(500, 1, true);
        this.builder = builder;
        this.pos = builder.getPos().add(offset);
        this.state = state;
        this.display = display;
    }

    @Override
    public boolean isReadyYet() {
        return builder.getWorld().isAirBlock(pos);
    }

    @Override
    public boolean isDone() {
        return builder.getWorld().getBlockState(pos) == state;
    }

    @Override
    protected void onRecievePower(int mJSoFar) {
        builder.startBlockBuilding(pos, display, 0);
    }
}
