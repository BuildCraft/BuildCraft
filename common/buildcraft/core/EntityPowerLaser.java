package buildcraft.core;

import net.minecraft.world.World;
import buildcraft.api.core.Position;

public class EntityPowerLaser extends EntityLaser {
	private String texture;

	public EntityPowerLaser(World world) {
		super(world);
	}

	public EntityPowerLaser(World world, Position head, Position tail) {
		super(world, head, tail);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(15, "");
	}

	@Override
	public String getTexture() {
		return texture;
	}

	public void setTexture(String texture) {
		this.texture = texture;
		needsUpdate = true;
	}

	@Override
	protected void updateDataClient() {
		super.updateDataClient();
		texture = dataWatcher.getWatchableObjectString(15);
	}

	@Override
	protected void updateDataServer() {
		super.updateDataServer();
		dataWatcher.updateObject(15, texture);
	}
}
