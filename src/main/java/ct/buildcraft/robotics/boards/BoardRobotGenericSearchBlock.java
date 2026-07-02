package ct.buildcraft.robotics.boards;

import java.util.ArrayList;
import java.util.List;

import ct.buildcraft.api.boards.RedstoneBoardRobot;
import ct.buildcraft.api.core.BlockIndex;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.api.robots.ResourceIdBlock;
import ct.buildcraft.robotics.ai.AIRobotGotoSleep;
import ct.buildcraft.robotics.ai.AIRobotSearchAndGotoBlock;
import ct.buildcraft.robotics.statements.ActionRobotFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;

public abstract class BoardRobotGenericSearchBlock extends RedstoneBoardRobot {
    private BlockIndex blockFound;
    private final List<Block> blockFilter = new ArrayList<>();

    public BoardRobotGenericSearchBlock(EntityRobotBase robot) {
        super(robot);
    }

    /** Must be safe to call from search code and avoid causing chunk loads. */
    public abstract boolean isExpectedBlock(Level level, BlockPos pos);

    @Override
    public void update() {
        updateFilter();
        startDelegateAI(new AIRobotSearchAndGotoBlock(robot, false, (level, pos) -> {
            if (!isExpectedBlock(level, pos)) {
                return false;
            }
            if (robot.getRegistry() != null && robot.getRegistry().isTaken(new ResourceIdBlock(pos))) {
                return false;
            }
            return matchesGateFilter(level, pos);
        }));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotSearchAndGotoBlock search) {
            if (search.success()) {
                blockFound = search.getBlockFound();
            } else {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        }
    }

    @Override
    public void end() {
        releaseBlockFound(true);
    }

    protected BlockIndex blockFound() {
        return blockFound;
    }

    protected void releaseBlockFound(boolean success) {
        if (blockFound != null) {
            if (robot.getRegistry() != null) {
                robot.getRegistry().release(new ResourceIdBlock(blockFound));
            }
            blockFound = null;
        }
    }

    public final void updateFilter() {
        blockFilter.clear();
        if (robot.getLinkedStation() == null) {
            return;
        }

        for (ItemStack stack : ActionRobotFilter.getGateFilterStacks(robot.getLinkedStation())) {
            if (stack.getItem() instanceof BlockItem blockItem) {
                blockFilter.add(blockItem.getBlock());
            }
        }
    }

    protected boolean matchesGateFilter(Level level, BlockPos pos) {
        return blockFilter.isEmpty() || blockFilter.contains(level.getBlockState(pos).getBlock());
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        if (blockFound != null) {
            CompoundTag tag = new CompoundTag();
            blockFound.writeTo(tag);
            nbt.put("indexStored", tag);
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        if (nbt.contains("indexStored")) {
            blockFound = new BlockIndex(nbt.getCompound("indexStored"));
        }
    }
}
