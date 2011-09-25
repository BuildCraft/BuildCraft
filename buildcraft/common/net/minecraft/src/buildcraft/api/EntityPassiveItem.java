package net.minecraft.src.buildcraft.api;

import net.minecraft.src.EntityItem;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class EntityPassiveItem {

	public float speed = 0.01F;
	public ItemStack item;
	
	public TileEntity container;
	
	public SafeTimeTracker synchroTracker = new SafeTimeTracker();
	
	public int deterministicRandomization = 0;
	
	public EntityPassiveItem(World world) {
		entityId = maxId;
		
		maxId = maxId + 1;
		
		if (maxId > Integer.MAX_VALUE) {
			maxId = 0;
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
		
	public EntityItem toEntityItem (World world, Orientations dir) {		
		if (!APIProxy.isClient(worldObj)) {
			Position motion = new Position (0, 0, 0, dir);
			motion.moveForwards(0.1 + speed * 2F);

			EntityItem entityitem = new EntityItem(world, posX, posY, posZ,
					item);

			float f3 = 0.00F + world.rand.nextFloat() * 0.04F - 0.02F;
			entityitem.motionX = (float) world.rand.nextGaussian() * f3 + motion.x;
			entityitem.motionY = (float) world.rand.nextGaussian() * f3 + motion.y;
			entityitem.motionZ = (float) world.rand.nextGaussian() * f3 + + motion.z;
			world.entityJoinedWorld(entityitem);

			entityitem.delayBeforeCanPickup = 20;
			return entityitem;
		} else {			
			return null;
		}
	}

}
