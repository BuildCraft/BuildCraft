package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.core.BlockIndex;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.api.robots.ResourceIdBlock;
import net.minecraft.nbt.CompoundTag;

public class AIRobotSearchAndGotoBlock extends AIRobot {
    private BlockIndex blockFound;
    private IBlockFilter filter;
    private boolean random;
    private double maxDistanceToEnd;

    public AIRobotSearchAndGotoBlock(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotSearchAndGotoBlock(EntityRobotBase robot, boolean random, IBlockFilter filter) {
        this(robot, random, filter, 0);
    }

    public AIRobotSearchAndGotoBlock(EntityRobotBase robot, boolean random, IBlockFilter filter, double maxDistanceToEnd) {
        this(robot);
        this.random = random;
        this.filter = filter;
        this.maxDistanceToEnd = maxDistanceToEnd;
    }

    @Override
    public void start() {
        startDelegateAI(new AIRobotSearchBlock(robot, random, filter, maxDistanceToEnd));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotSearchBlock search) {
            if (search.success() && search.takeResource()) {
                blockFound = search.blockFound;
                startDelegateAI(new AIRobotGotoBlock(robot, search.path));
            } else {
                setSuccess(false);
                terminate();
            }
        } else if (ai instanceof AIRobotGotoBlock) {
            if (!ai.success()) {
                releaseBlockFound();
                setSuccess(false);
            }
            terminate();
        }
    }

    private void releaseBlockFound() {
        if (blockFound != null && robot.getRegistry() != null) {
            robot.getRegistry().release(new ResourceIdBlock(blockFound));
            blockFound = null;
        }
    }

    public BlockIndex getBlockFound() {
        return blockFound;
    }

    @Override
    public boolean success() {
        return blockFound != null;
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
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
