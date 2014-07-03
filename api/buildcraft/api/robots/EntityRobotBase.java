/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.robots;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.core.IBox;

public abstract class EntityRobotBase extends EntityLiving implements IInventory {

	public static double MAX_ENERGY = 10000;

	public EntityRobotBase(World par1World) {
		super(par1World);
	}

	public abstract void setItemInUse(ItemStack stack);

	public abstract void setItemActive(boolean b);

	public abstract boolean isMoving();

	public abstract IDockingStation getLinkedStation();

	public abstract IDockingStation getReservedStation();

	public abstract RedstoneBoardRobot getBoard();

	public abstract void aimItemAt(int x, int y, int z);

	public abstract double getEnergy();

	public abstract void setEnergy(double energy);

	public abstract IDockingStation getDockingStation();

	public abstract void dock(IDockingStation station);

	public abstract void undock();

	public abstract boolean reserveStation(IDockingStation station);

	public abstract boolean linkToStation(IDockingStation station);

	public abstract IBox getAreaToWork();

	public abstract boolean containsItems();

	public abstract void unreachableEntityDetected(Entity entity);

	public abstract boolean isKnownUnreachable(Entity entity);

}
