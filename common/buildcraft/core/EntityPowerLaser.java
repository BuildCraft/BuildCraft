package buildcraft.core;

import buildcraft.api.core.Position;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityPowerLaser extends EntityLaser {

	private byte texture;

	public EntityPowerLaser(World world) {
		super(world);
	}

	public EntityPowerLaser(World world, Position head, Position tail) {
		super(world, head, tail);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(15, (byte) 0);
	}

	@Override
	public ResourceLocation getTexture() {
		return LASER_TEXTURES[texture];
	}

	public void setTexture(int texture) {
		this.texture = (byte) texture;
		needsUpdate = true;
	}

	@Override
	protected void updateDataClient() {
		super.updateDataClient();
		texture = dataWatcher.getWatchableObjectByte(15);
	}

	@Override
	protected void updateDataServer() {
		super.updateDataServer();
		dataWatcher.updateObject(15, texture);
	}
}
