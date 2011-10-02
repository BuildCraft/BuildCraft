/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 * 
 * As a special exception, this file is part of the BuildCraft API and is 
 * allowed to be redistributed, either in source or binaries form.
 */

package net.minecraft.src.buildcraft.api;

import java.util.TreeMap;

import net.minecraft.src.EntityItem;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class EntityPassiveItem {
	
	public static TreeMap<Integer, EntityPassiveItem> allEntities = new TreeMap<Integer, EntityPassiveItem>();

	public float speed = 0.01F;
	public ItemStack item;
	
	public TileEntity container;
	
	public SafeTimeTracker synchroTracker = new SafeTimeTracker();
	
	public int deterministicRandomization = 0;
	
	public EntityPassiveItem(World world) {
		this (world, maxId != Integer.MAX_VALUE ? ++maxId : (maxId = 0));		
	}
	
	public EntityPassiveItem(World world, int id) {		
		entityId = id;
		allEntities.put(entityId, this);
		worldObj = world;
	}
	
	public static EntityPassiveItem getOrCreate (World world, int id) {
		if (allEntities.containsKey(id)) {
			return allEntities.get(id);
		} else {
			return new EntityPassiveItem(world, id);
		}
	}
	
	World worldObj;
	public double posX, posY, posZ;
	public int entityId;
	
	private static int maxId = 0;
	
	public EntityPassiveItem(World world, double d, double d1, double d2) {	
		this (world);
		posX = d;
		posY = d1;
		posZ = d2;
		worldObj = world;
	}
	
	public void setPosition (double x, double y, double z) {
		posX = x;
		posY = y;
		posZ = z;
	}

	public EntityPassiveItem(World world, double d, double d1, double d2, 
			ItemStack itemstack) {
		this (world, d, d1, d2);
		this.item = itemstack.copy();		
    }

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		posX = nbttagcompound.getDouble("x");
		posY = nbttagcompound.getDouble("y");
		posZ = nbttagcompound.getDouble("z");		
		speed = nbttagcompound.getFloat("speed");
		item = ItemStack.loadItemStackFromNBT(nbttagcompound.getCompoundTag("Item"));
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {		
		nbttagcompound.setDouble("x", posX);
		nbttagcompound.setDouble("y", posY);
		nbttagcompound.setDouble("z", posZ);
		nbttagcompound.setFloat("speed", speed);
		NBTTagCompound nbttagcompound2 = new NBTTagCompound();
		item.writeToNBT(nbttagcompound2);
		nbttagcompound.setCompoundTag("Item", nbttagcompound2);
	}
		
	public EntityItem toEntityItem (Orientations dir) {		
		if (!APIProxy.isClient(worldObj)) {
			Position motion = new Position (0, 0, 0, dir);
			motion.moveForwards(0.1 + speed * 2F);

			EntityItem entityitem = new EntityItem(worldObj, posX, posY, posZ,
					item);

			float f3 = 0.00F + worldObj.rand.nextFloat() * 0.04F - 0.02F;
			entityitem.motionX = (float) worldObj.rand.nextGaussian() * f3 + motion.x;
			entityitem.motionY = (float) worldObj.rand.nextGaussian() * f3 + motion.y;
			entityitem.motionZ = (float) worldObj.rand.nextGaussian() * f3 + + motion.z;
			worldObj.entityJoinedWorld(entityitem);
			remove ();

			entityitem.delayBeforeCanPickup = 20;
			return entityitem;
		} else {			
			return null;
		}
	}
	
	public void remove () {
		if (allEntities.containsKey(entityId)) {
			allEntities.remove(entityId);
		}
	}

}
