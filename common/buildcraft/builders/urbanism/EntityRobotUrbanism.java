/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.urbanism;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import buildcraft.core.robots.EntityRobot;

public class EntityRobotUrbanism extends EntityRobot {

	UrbanistTask task;

	public EntityRobotUrbanism(World par1World) {
		super(par1World);
	}

	public boolean isAvailable () {
		return task == null;
	}

	public void setTask (UrbanistTask task) {
		this.task = task;

		if (!worldObj.isRemote) {
			task.setup(this);
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (!worldObj.isRemote) {
			if (task != null) {
				task.update(this);

				if (task.done()) {
					task = null;
				}
			}
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
		this.setDead();
	}
}
