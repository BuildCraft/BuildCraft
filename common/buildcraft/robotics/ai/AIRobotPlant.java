package buildcraft.robotics.ai;

import buildcraft.api.crops.CropManager;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.misc.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public class AIRobotPlant extends AIRobot {
    private BlockPos blockFound;
    private int delay = 0;

    public AIRobotPlant(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotPlant(EntityRobotBase iRobot, BlockPos iBlockFound) {
        this(iRobot);

        blockFound = iBlockFound;
    }

    @Override
    public void start() {
        robot.aimItemAt(blockFound);
        robot.setItemActive(true);
    }

    @Override
    public void update() {
        if (blockFound == null) {
            setSuccess(false);
            terminate();
        }

        if (delay++ > 40) {
            Player player = FakePlayerProvider.INSTANCE.getFakePlayer((ServerLevel) robot.level, FakePlayerProvider.NULL_PROFILE);
            if (CropManager.plantCrop(robot.level, player, robot.getMainHandItem(), blockFound)) {
            } else {
                setSuccess(false);
            }
            if (robot.getMainHandItem().getCount() > 0) {
                BlockUtil.dropItem((ServerLevel) robot.level, VecUtil.getPos(robot), 6000, robot.getMainHandItem());
            }
            robot.setItemInUse(StackUtil.EMPTY);
            terminate();
        }
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
        super.writeSelfToNBT(nbt);

        if (blockFound != null) {
            nbt.put("blockFound", NBTUtilBC.writeBlockPos(blockFound));
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        super.loadSelfFromNBT(nbt);

        if (nbt.contains("blockFound")) {
            blockFound = NBTUtilBC.readBlockPos(nbt.get("blockFound"));
        }
    }
}
