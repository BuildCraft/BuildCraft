/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import java.util.HashSet;
import java.util.Set;

import buildcraft.api.core.SafeTimeTracker;
import buildcraft.core.DefaultProps;
import buildcraft.core.proxy.CoreProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityRobotPicker extends EntityRobot {

	private static ResourceLocation texture = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_ENTITIES + "/robot_picker.png");

	SafeTimeTracker scanTracker = new SafeTimeTracker(40, 10);
	SafeTimeTracker pickTracker = new SafeTimeTracker(20, 0);
	int pickTime = -1;

	public EntityRobotPicker(World par1World) {
		super(par1World);
	}

	@Override
	public ResourceLocation getTexture () {
		return texture;
	}

	private static Set <Integer> targettedItems = new HashSet<Integer>();
	private EntityItem target;

	@Override
	public void onUpdate () {
		super.onUpdate();

		if (CoreProxy.proxy.isRenderWorld(worldObj)) {
			return;
		}

		if (target != null) {
			if (target.isDead) {
				targettedItems.remove(target.entityId);
				target = null;
				currentAI = new AIReturnToDock();
				hideLaser();
				scan ();
			} else if (pickTime == -1){
				if (getDistance(target.posX, target.posY, target.posZ) < 10) {
					setLaserDestination((float) target.posX, (float) target.posY, (float) target.posZ);
					showLaser();
					pickTracker = new SafeTimeTracker (200);
					pickTime = 0;
				}
			} else {
				pickTime++;

				if (pickTime > 20) {
					target.setDead();
				}
			}
		} else {
			if (scanTracker.markTimeIfDelay(worldObj)) {
				scan ();
			}
		}
	}


	public void scan () {
		for (Object o : worldObj.loadedEntityList) {
			Entity e = (Entity) o;

			if (!e.isDead && e instanceof EntityItem && !targettedItems.contains(e.entityId)) {
				double dx = e.posX - posX;
				double dy = e.posY - posY;
				double dz = e.posZ - posZ;

				double sqrDistance = dx * dx + dy * dy + dz * dz;
				double maxDistance = 100 * 100;

				if (sqrDistance <= maxDistance) {
					EntityItem item = (EntityItem) e;
					target = item;
					targettedItems.add(e.entityId);
					currentAI = new AIMoveAround(this, (float) e.posX, (float) e.posY, (float) e.posZ);
					pickTime = -1;
					break;
				}
			}
		}
	}
}
