package ct.buildcraft.robotics.boards;

import ct.buildcraft.api.boards.RedstoneBoardRobotNBT;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.robotics.BCRoboticsBoards;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;

/** BuildCraft 7.1.x shovelman board port. Fetches a shovel and digs shovel-mineable blocks in the work zone. */
public class BoardRobotShovelman extends BoardRobotGenericBreakBlock {
    public BoardRobotShovelman(EntityRobotBase robot) {
        super(robot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCRoboticsBoards.getByKey("shovelman").nbt();
    }

    @Override
    public boolean isExpectedTool(ItemStack stack) {
        return !stack.isEmpty()
                && (stack.getItem() instanceof ShovelItem || stack.canPerformAction(ToolActions.SHOVEL_DIG));
    }

    @Override
    public boolean isExpectedBlock(Level level, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0F || !state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return false;
        }

        ItemStack held = robot.getItemBySlot(EquipmentSlot.MAINHAND);
        return isExpectedTool(held) && held.isCorrectToolForDrops(state);
    }
}
