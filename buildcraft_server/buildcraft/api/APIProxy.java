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
import net.minecraft.src.World;

public class APIProxy {

	public static boolean isClient(World world) {
		return false;
	}

	public static boolean isServerSide() {
		return true;
	}

	public static boolean isRemote() {
		return false;
	}

	public static void removeEntity(Entity entity) {
		entity.setDead();
	}

	public static Random createNewRandom(World world) {
		return new Random(world.getSeed());
	}

	public static EntityPlayer createNewPlayer(World world) {
		return new EntityPlayer(world) {

		};
	}
}
