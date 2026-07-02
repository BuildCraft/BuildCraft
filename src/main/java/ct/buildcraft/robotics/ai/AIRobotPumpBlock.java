package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.core.BlockIndex;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.lib.misc.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

/** Pumps one source fluid block into the robot's internal tank, matching the classic BuildCraft pump robot cadence. */
public class AIRobotPumpBlock extends AIRobot {
    private BlockIndex blockToPump;
    private long waited;
    private int pumped;

    public AIRobotPumpBlock(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotPumpBlock(EntityRobotBase robot, BlockIndex blockToPump) {
        this(robot);
        this.blockToPump = blockToPump;
    }

    @Override
    public void start() {
        if (blockToPump == null) {
            setSuccess(false);
            terminate();
            return;
        }
        robot.aimItemAt(blockToPump.toBlockPos());
        robot.setItemActive(true);
    }

    @Override
    public void update() {
        if (blockToPump == null) {
            setSuccess(false);
            terminate();
            return;
        }

        if (waited < 40) {
            waited++;
            return;
        }

        BlockPos pos = blockToPump.toBlockPos();
        if (!robot.level.isLoaded(pos)) {
            setSuccess(false);
            terminate();
            return;
        }

        FluidStack simulated = BlockUtil.drainBlock(robot.level, pos, false);
        if (!simulated.isEmpty()) {
            int accepted = robot.fill(simulated, FluidAction.SIMULATE);
            if (accepted > 0) {
                FluidStack drained = BlockUtil.drainBlock(robot.level, pos, true);
                if (!drained.isEmpty()) {
                    drained.setAmount(Math.min(drained.getAmount(), accepted));
                    pumped = robot.fill(drained, FluidAction.EXECUTE);
                }
            }
        }

        setSuccess(pumped > 0);
        terminate();
    }

    @Override
    public void end() {
        robot.setItemActive(false);
    }

    @Override
    public int getEnergyCost() {
        return 5;
    }

    @Override
    public boolean success() {
        return pumped > 0;
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        if (blockToPump != null) {
            CompoundTag tag = new CompoundTag();
            blockToPump.writeTo(tag);
            nbt.put("blockToPump", tag);
        }
        nbt.putLong("waited", waited);
        nbt.putInt("pumped", pumped);
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        if (nbt.contains("blockToPump")) {
            blockToPump = new BlockIndex(nbt.getCompound("blockToPump"));
        }
        waited = nbt.getLong("waited");
        pumped = nbt.getInt("pumped");
    }
}
