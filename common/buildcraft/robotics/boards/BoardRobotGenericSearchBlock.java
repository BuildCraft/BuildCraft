package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.core.IBlockFilter;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.ResourceIdBlock;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.api.statements.StatementSlot;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotSearchAndGotoBlock;
import buildcraft.robotics.statements.ActionRobotFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

public abstract class BoardRobotGenericSearchBlock extends RedstoneBoardRobot {

    private BlockPos blockFound;
    private ArrayList<BlockState> blockFilter = new ArrayList<BlockState>();

    public BoardRobotGenericSearchBlock(EntityRobotBase iRobot) {
        super(iRobot);
    }

    /** This function has to be derived in a thread safe manner, as it may be called from parallel jobs. In particular,
     * world should not be directly used, only through WorldProperty class and subclasses. */
    public abstract boolean isExpectedBlock(Level world, BlockPos pos);

    @Override
    public void update() {
        updateFilter();

        startDelegateAI(new AIRobotSearchAndGotoBlock(robot, false, new IBlockFilter() {
            @Override
            public boolean matches(Level world, BlockPos pos) {
                if (isExpectedBlock(world, pos) && !robot.getRegistry().isTaken(new ResourceIdBlock(pos))) {
                    return matchesGateFilter(world, pos);
                } else {
                    return false;
                }
            }
        }));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotSearchAndGotoBlock) {
            if (ai.success()) {
                blockFound = ((AIRobotSearchAndGotoBlock) ai).getBlockFound();
            } else {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        }
    }

    @Override
    public void end() {
        releaseBlockFound(true);
    }

    protected BlockPos blockFound() {
        return blockFound;
    }

    protected void releaseBlockFound(boolean success) {
        if (blockFound != null) {
            // TODO: if !ai.success() -> can't break block, blacklist it
            robot.getRegistry().release(new ResourceIdBlock(blockFound));
            blockFound = null;
        }
    }

    public final void updateFilter() {
        blockFilter.clear();

        for (StatementSlot slot : robot.getLinkedStation().getActiveActions()) {
            if (slot.statement instanceof ActionRobotFilter) {
                for (IStatementParameter p : slot.parameters) {
                    // if (p != null && p instanceof StatementParameterItemStack)
                    if (p instanceof StatementParameterItemStack) {
                        StatementParameterItemStack param = (StatementParameterItemStack) p;
                        ItemStack stack = param.getItemStack();

                        if (stack != null && stack.getItem() instanceof BlockItem) {
                            BlockItem item = (BlockItem) stack.getItem();
                            // blockFilter.add(item.block.getStateFromMeta(stack.getMetadata()));
                            blockFilter.add(item.getBlock().defaultBlockState());
                        }
                    }
                }
            }
        }
    }

    protected boolean matchesGateFilter(Level world, BlockPos pos) {
        if (blockFilter.size() == 0) {
            return true;
        }

        BlockState state;
        int meta;
        synchronized (world) {
            state = world.getBlockState(pos);
        }

        for (BlockState filter : blockFilter) {
            if (filter == state) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        super.writeSelfToNBT(nbt);

        if (blockFound != null) {
            nbt.put("indexStored", NBTUtilBC.writeBlockPos(blockFound));
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        super.loadSelfFromNBT(nbt);

        if (nbt.contains("indexStored")) {
            blockFound = NBTUtilBC.readBlockPos(nbt.get("indexStored"));
        }
    }

}
