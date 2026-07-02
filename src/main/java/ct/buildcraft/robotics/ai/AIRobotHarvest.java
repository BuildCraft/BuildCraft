package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.core.BlockIndex;
import ct.buildcraft.api.crops.CropManager;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.lib.misc.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/** Harvests one mature crop block and drops the crop output at the robot. Ported from BuildCraft 7.1.x AIRobotHarvest. */
public class AIRobotHarvest extends AIRobot {
    private BlockIndex blockFound;
    private int delay;

    public AIRobotHarvest(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotHarvest(EntityRobotBase robot, BlockIndex blockFound) {
        this(robot);
        this.blockFound = blockFound;
    }

    @Override
    public void start() {
        if (blockFound != null) {
            robot.aimItemAt(blockFound.toBlockPos());
            robot.setItemActive(true);
        }
    }

    @Override
    public void update() {
        if (!(robot.level instanceof ServerLevel serverLevel) || blockFound == null) {
            setSuccess(false);
            terminate();
            return;
        }

        if (delay++ <= 20) {
            return;
        }

        BlockPos pos = blockFound.toBlockPos();
        if (!CropManager.isMature(serverLevel, serverLevel.getBlockState(pos), pos)) {
            setSuccess(false);
            terminate();
            return;
        }

        NonNullList<ItemStack> drops = NonNullList.create();
        if (!CropManager.harvestCrop(serverLevel, pos, drops)) {
            setSuccess(false);
            terminate();
            return;
        }

        for (ItemStack stack : drops) {
            if (!stack.isEmpty()) {
                BlockUtil.dropItem(serverLevel, robot.blockPosition(), 6000, stack.copy());
            }
        }
        terminate();
    }

    @Override
    public void end() {
        robot.setItemActive(false);
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
            nbt.put("blockFound", tag);
        }
        nbt.putInt("delay", delay);
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        if (nbt.contains("blockFound")) {
            blockFound = new BlockIndex(nbt.getCompound("blockFound"));
        }
        delay = nbt.getInt("delay");
    }
}
