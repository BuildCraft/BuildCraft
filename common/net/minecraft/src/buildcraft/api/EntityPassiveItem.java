/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.api;

import java.util.TreeMap;

import net.minecraft.src.EntityItem;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.MathHelper;
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
			worldObj.spawnEntityInWorld(entityitem);
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
	
	public float getEntityBrightness(float f)
    {
        int i = MathHelper.floor_double(posX);
        int j = MathHelper.floor_double(posZ);
        worldObj.getClass();
        if(worldObj.blockExists(i, 128 / 2, j))
        {
            double d = 0.66000000000000003D;
            int k = MathHelper.floor_double(posY + d);
            return worldObj.getLightBrightness(i, k, j);
        } else
        {
            return 0.0F;
        }
    }
	
	public boolean isCorrupted () {
		return item == null || item.stackSize <= 0
				|| Item.itemsList[item.itemID] == null;
	}

}
