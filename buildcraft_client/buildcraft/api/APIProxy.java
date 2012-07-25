/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.api;

import java.util.Random;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import net.minecraft.src.WorldClient;

public class APIProxy {

	public static boolean isClient(World world) {
		return world instanceof WorldClient;
	}

	public static boolean isServerSide() {
		return false;
	}

	public static boolean isRemote() {
		return ModLoader.getMinecraftInstance().theWorld.isRemote;
	}

	public static void removeEntity(Entity entity) {
		entity.setDead();

		if (entity.worldObj instanceof WorldClient)
			((WorldClient) entity.worldObj).removeEntityFromWorld(entity.entityId);
	}

	public static Random createNewRandom(World world) {
		return new Random(world.getSeed());
	}

	public static EntityPlayer createNewPlayer(World world) {
		return new EntityPlayer(world) {

			@Override
			public void func_6420_o() {
				// TODO Auto-generated method stub

			}
		};
	}
}
