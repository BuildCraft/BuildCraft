package buildcraft.robotics.boards;

import java.util.ArrayList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.ResourceIdBlock;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.api.statements.StatementSlot;
import buildcraft.core.lib.utils.IBlockFilter;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotSearchAndGotoBlock;
import buildcraft.robotics.statements.ActionRobotFilter;

public abstract class BoardRobotGenericSearchBlock extends RedstoneBoardRobot {

    private BlockPos blockFound;
    private ArrayList<IBlockState> blockFilter = new ArrayList<IBlockState>();

    public BoardRobotGenericSearchBlock(EntityRobotBase iRobot) {
        super(iRobot);
    }

    /** This function has to be derived in a thread safe manner, as it may be called from parallel jobs. In particular,
     * world should not be directly used, only through WorldProperty class and subclasses. */
    public abstract boolean isExpectedBlock(World world, BlockPos pos);

    @Override
    public void update() {
        updateFilter();

        startDelegateAI(new AIRobotSearchAndGotoBlock(robot, false, new IBlockFilter() {
            @Override
            public boolean matches(World world, BlockPos pos) {
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
                    if (p != null && p instanceof StatementParameterItemStack) {
                        StatementParameterItemStack param = (StatementParameterItemStack) p;
                        ItemStack stack = param.getItemStack();

                        if (stack != null && stack.getItem() instanceof ItemBlock) {
                            ItemBlock item = (ItemBlock) stack.getItem();
                            blockFilter.add(item.block.getStateFromMeta(stack.getMetadata()));
                        }
                    }
                }
            }
        }
    }

    protected boolean matchesGateFilter(World world, BlockPos pos) {
        if (blockFilter.size() == 0) {
            return true;
        }

        IBlockState state;
        int meta;
        synchronized (world) {
            state = world.getBlockState(pos);
        }

        for (IBlockState filter : blockFilter) {
            if (filter == state) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void writeSelfToNBT(NBTTagCompound nbt) {
        super.writeSelfToNBT(nbt);

        if (blockFound != null) {
            nbt.setTag("indexStored", NBTUtils.writeBlockPos(blockFound));
        }
    }

    @Override
    public void loadSelfFromNBT(NBTTagCompound nbt) {
        super.loadSelfFromNBT(nbt);

        if (nbt.hasKey("indexStored")) {
            blockFound = NBTUtils.readBlockPos(nbt.getTag("indexStored"));
        }
    }

}
