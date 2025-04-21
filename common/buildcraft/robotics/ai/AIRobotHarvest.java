package buildcraft.robotics.ai;

import buildcraft.api.crops.CropManager;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.VecUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

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
            // if (!BuildCraftAPI.getWorldProperty("harvestable").get(robot.level, blockFound))
            if (!CropManager.isMature(robot.level, robot.level.getBlockState(blockFound), blockFound)) {
                setSuccess(false);
                terminate();
                return;
            }
            NonNullList<ItemStack> drops = NonNullList.create();
            // if (!CropManager.harvestCrop(robot.level, blockFound, drops))
            CropManager.HarvestResult result = CropManager.harvestCrop(robot.level, blockFound, robot.getMainHandItem(), drops);
            if (result == CropManager.HarvestResult.FAIL) {
                setSuccess(false);
                terminate();
                return;
            } else if (result == CropManager.HarvestResult.PROGRESS) {
                startDelegateAI(new AIRobotBreak(robot, blockFound));
            } else {
                for (ItemStack stack : drops) {
                    BlockUtil.dropItem((ServerLevel) robot.level, VecUtil.getPos(robot), 6000, stack);
                }
            }
        }
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
