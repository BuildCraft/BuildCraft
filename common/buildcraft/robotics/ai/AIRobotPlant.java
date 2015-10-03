package buildcraft.robotics.ai;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldServer;

import buildcraft.api.crops.CropManager;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.proxy.CoreProxy;

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
            EntityPlayer player = CoreProxy.proxy.getBuildCraftPlayer((WorldServer) robot.worldObj).get();
            if (CropManager.plantCrop(robot.worldObj, player, robot.getHeldItem(), blockFound)) {} else {
                setSuccess(false);
            }
            if (robot.getHeldItem().stackSize > 0) {
                BlockUtils.dropItem((WorldServer) robot.worldObj, Utils.getPos(robot), 6000, robot.getHeldItem());
            }
            robot.setItemInUse(null);
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
    public void writeSelfToNBT(NBTTagCompound nbt) {
        super.writeSelfToNBT(nbt);

        if (blockFound != null) {
            nbt.setTag("blockFound", NBTUtils.writeBlockPos(blockFound));
        }
    }

    @Override
    public void loadSelfFromNBT(NBTTagCompound nbt) {
        super.loadSelfFromNBT(nbt);

        if (nbt.hasKey("blockFound")) {
            blockFound = NBTUtils.readBlockPos(nbt.getTag("blockFound"));
        }
    }
}
