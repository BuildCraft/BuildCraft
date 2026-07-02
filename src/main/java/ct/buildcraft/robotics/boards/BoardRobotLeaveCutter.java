package ct.buildcraft.robotics.boards;

import ct.buildcraft.api.boards.RedstoneBoardRobotNBT;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.robotics.BCRoboticsBoards;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;

/** BuildCraft 7.1.x leaf cutter board port. Fetches shears and cuts leaf blocks. */
public class BoardRobotLeaveCutter extends BoardRobotGenericBreakBlock {
    public BoardRobotLeaveCutter(EntityRobotBase robot) {
        super(robot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCRoboticsBoards.getByKey("leave_cutter").nbt();
    }

    @Override
    public boolean isExpectedTool(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ShearsItem;
    }

    @Override
    public boolean isExpectedBlock(Level level, BlockPos pos) {
        return level.isLoaded(pos) && level.getBlockState(pos).is(BlockTags.LEAVES);
    }
}
