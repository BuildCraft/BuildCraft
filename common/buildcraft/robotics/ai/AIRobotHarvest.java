package buildcraft.robotics.ai;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.crops.CropManager;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.utils.BlockUtils;

public class AIRobotHarvest extends AIRobot {

	private BlockIndex blockFound;
	private int delay = 0;

	public AIRobotHarvest(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotHarvest(EntityRobotBase iRobot, BlockIndex iBlockFound) {
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
			if (!BuildCraftAPI.getWorldProperty("harvestable").get(robot.worldObj, blockFound.x,
					blockFound.y, blockFound.z)) {
				setSuccess(false);
				terminate();
				return;
			}
			List<ItemStack> drops = new ArrayList<ItemStack>();
			if (!CropManager.harvestCrop(robot.worldObj, blockFound.x, blockFound.y, blockFound.z,
					drops)) {
				setSuccess(false);
				terminate();
				return;
			}
			for (ItemStack stack : drops) {
				BlockUtils.dropItem((WorldServer) robot.worldObj,
						MathHelper.floor_double(robot.posX), MathHelper.floor_double(robot.posY),
						MathHelper.floor_double(robot.posZ), 6000, stack);
			}
		}
	}

	@Override
	public boolean canLoadFromNBT() {
		return true;
	}

	@Override
	public void writeSelfToNBT(NBTTagCompound nbt) {
		super.writeSelfToNBT(nbt);

		if (blockFound != null) {
			NBTTagCompound sub = new NBTTagCompound();
			blockFound.writeTo(sub);
			nbt.setTag("blockFound", sub);
		}
	}

	@Override
	public void loadSelfFromNBT(NBTTagCompound nbt) {
		super.loadSelfFromNBT(nbt);

		if (nbt.hasKey("blockFound")) {
			blockFound = new BlockIndex(nbt.getCompoundTag("blockFound"));
		}
	}
}
