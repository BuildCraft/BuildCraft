package buildcraft.builders.urbanism;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import buildcraft.core.EntityRobot;
import buildcraft.core.proxy.CoreProxy;

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

		if (CoreProxy.proxy.isSimulating(worldObj)) {
			task.setup(this);
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (CoreProxy.proxy.isSimulating(worldObj)) {
			if (task != null) {
				task.work(this);

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
