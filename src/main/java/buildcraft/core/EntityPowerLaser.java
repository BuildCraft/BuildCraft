/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
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
