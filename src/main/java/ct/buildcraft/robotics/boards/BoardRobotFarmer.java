package ct.buildcraft.robotics.boards;

import ct.buildcraft.api.boards.RedstoneBoardRobot;
import ct.buildcraft.api.boards.RedstoneBoardRobotNBT;
import ct.buildcraft.api.core.BlockIndex;
import ct.buildcraft.api.core.IStackFilter;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.api.robots.ResourceIdBlock;
import ct.buildcraft.robotics.BCRoboticsBoards;
import ct.buildcraft.robotics.ai.AIRobotFetchAndEquipItemStack;
import ct.buildcraft.robotics.ai.AIRobotGotoSleep;
import ct.buildcraft.robotics.ai.AIRobotSearchAndGotoBlock;
import ct.buildcraft.robotics.ai.AIRobotUseToolOnBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** BuildCraft 7.1.x farmer board port. Fetches a hoe, finds tillable dirt-like blocks, and hoes them. */
public class BoardRobotFarmer extends RedstoneBoardRobot {
    private BlockIndex blockFound;

    public BoardRobotFarmer(EntityRobotBase robot) {
        super(robot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCRoboticsBoards.getByKey("farmer").nbt();
    }

    @Override
    public void update() {
        ItemStack held = robot.getItemBySlot(EquipmentSlot.MAINHAND);
        if (!isHoe(held)) {
            startDelegateAI(new AIRobotFetchAndEquipItemStack(robot, new HoeFilter()));
        } else {
            startDelegateAI(new AIRobotSearchAndGotoBlock(robot, false, this::isExpectedBlock));
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotSearchAndGotoBlock search) {
            if (search.success()) {
                blockFound = search.getBlockFound();
                startDelegateAI(new AIRobotUseToolOnBlock(robot, blockFound));
            } else {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotFetchAndEquipItemStack) {
            if (!ai.success()) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotUseToolOnBlock) {
            releaseBlockFound();
        }
    }

    @Override
    public void end() {
        releaseBlockFound();
    }

    private boolean isExpectedBlock(Level level, BlockPos pos) {
        if (!level.isLoaded(pos) || !isAirAbove(level, pos)) {
            return false;
        }
        if (robot.getRegistry() != null && robot.getRegistry().isTaken(new ResourceIdBlock(pos))) {
            return false;
        }
        return isTillable(level.getBlockState(pos));
    }

    private static boolean isTillable(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.PODZOL);
    }

    private static boolean isAirAbove(Level level, BlockPos pos) {
        return level.isEmptyBlock(pos.above());
    }

    private void releaseBlockFound() {
        if (blockFound != null) {
            if (robot.getRegistry() != null) {
                robot.getRegistry().release(new ResourceIdBlock(blockFound));
            }
            blockFound = null;
        }
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        if (blockFound != null) {
            CompoundTag tag = new CompoundTag();
            blockFound.writeTo(tag);
            nbt.put("blockFound", tag);
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        if (nbt.contains("blockFound")) {
            blockFound = new BlockIndex(nbt.getCompound("blockFound"));
        }
    }

    private static boolean isHoe(ItemStack stack) {
        return !stack.isEmpty()
                && (!stack.isDamageableItem() || stack.getDamageValue() < stack.getMaxDamage())
                && stack.getItem() instanceof HoeItem;
    }

    private static final class HoeFilter implements IStackFilter {
        @Override
        public boolean matches(ItemStack stack) {
            return isHoe(stack);
        }
    }
}
