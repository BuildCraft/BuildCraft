/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robots;

import net.minecraft.entity.EntityLiving;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import buildcraft.api.boards.RedstoneBoardRobot;

public abstract class EntityRobotBase extends EntityLiving implements IInventory {

	public static double MAX_ENERGY = 10000;

	public EntityRobotBase(World par1World) {
		super(par1World);
	}

	public abstract void setItemInUse(ItemStack stack);

	public abstract ItemStack getItemInUse();

	public abstract void setItemActive(boolean b);

	public abstract boolean isMoving();

	public abstract void setCurrentDockingStation(DockingStation station);

	public abstract DockingStation getCurrentDockingStation();

	public abstract DockingStation getMainDockingStation();

	public abstract RedstoneBoardRobot getBoard();

	public abstract void aimItemAt(int x, int y, int z);

	public abstract double getEnergy();

	public abstract void setEnergy(double energy);

}
