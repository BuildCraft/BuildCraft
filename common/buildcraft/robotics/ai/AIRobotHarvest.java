package buildcraft.robotics.ai;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldServer;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.crops.CropManager;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.core.lib.utils.Utils;

public class AIRobotHarvest extends AIRobot {

    private BlockPos blockFound;
    private int delay = 0;

    public AIRobotHarvest(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotHarvest(EntityRobotBase iRobot, BlockPos iBlockFound) {
        super(iRobot);
        blockFound = iBlockFound;
    }

    @Override
    public void update() {
        if (blockFound == null) {
            setSuccess(false);
            terminate();
            return;
        }

        if (delay++ > 20) {
            if (!BuildCraftAPI.getWorldProperty("harvestable").get(robot.worldObj, blockFound)) {
                setSuccess(false);
                terminate();
                return;
            }
            List<ItemStack> drops = new ArrayList<ItemStack>();
            if (!CropManager.harvestCrop(robot.worldObj, blockFound, drops)) {
                setSuccess(false);
                terminate();
                return;
            }
            for (ItemStack stack : drops) {
                BlockUtils.dropItem((WorldServer) robot.worldObj, Utils.getPos(robot), 6000, stack);
            }
        }
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
