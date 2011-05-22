package net.minecraft.src.buildcraft.core;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;

public class EntityPassiveItem extends Entity {

	public float speed = 0.01F;
	public ItemStack item;
	
	public EntityPassiveItem(World world) {
		super(world);				
    	
    	noClip = true;
	}
	
	public EntityPassiveItem(World world, double d, double d1, double d2) {
		super (world);
	}
	
    public EntityPassiveItem(World world, double d, double d1, double d2, 
            ItemStack itemstack) {
    	this (world);
    	this.item = itemstack;
    	
		 setSize(0.25F, 0.25F);
		 setPosition(d, d1, d2);

    	//  super (world, d, d1, d2, itemstack);
    	
    	//  CoreProxy.setField804(this, 0);
    	noClip = true;
    }

	@Override
	protected void entityInit() {
		// TODO Auto-generated method stub
		
	}
	
	public void onUpdate() {
		//super.onUpdate();
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		//= super.readEntityFromNBT(nbttagcompound);
		
		posX = nbttagcompound.getDouble("x");
		posY = nbttagcompound.getDouble("y");
		posZ = nbttagcompound.getDouble("z");		
		speed = nbttagcompound.getFloat("speed");
		item = new ItemStack(nbttagcompound.getCompoundTag("Item"));
		
		setPosition (posX, posY, posZ);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {		
		// super.writeEntityToNBT(nbttagcompound);
		
		nbttagcompound.setDouble("x", posX);
		nbttagcompound.setDouble("y", posY);
		nbttagcompound.setDouble("z", posZ);
		nbttagcompound.setFloat("speed", speed);
		NBTTagCompound nbttagcompound2 = new NBTTagCompound();
		item.writeToNBT(nbttagcompound2);
		nbttagcompound.setCompoundTag("Item", nbttagcompound2);
	}
	
	@Override
	public void onCollideWithPlayer(EntityPlayer entityplayer) {
		
	}
	
	@Override
	public boolean attackEntityFrom(Entity entity, int i) {
	   return false;
	}
	
	@Override
	public boolean handleWaterMovement() {
		return false;
	 }
	
	public EntityItem toEntityItem (World world, Orientations dir) {
		setEntityDead();		
		
		Position motion = new Position (0, 0, 0, dir);
		motion.moveForwards(0.1 + speed * 2F);
							
		EntityItem entityitem = new EntityItem(world, posX, posY, posZ,
				item);

		float f3 = 0.00F + world.rand.nextFloat() * 0.04F - 0.02F;
		entityitem.motionX = (float) world.rand.nextGaussian() * f3 + motion.x;
		entityitem.motionY = (float) world.rand.nextGaussian() * f3
				+ + motion.y;
		entityitem.motionZ = (float) world.rand.nextGaussian() * f3 + + motion.z;
		world.entityJoinedWorld(entityitem);
		
		return entityitem;
	}

}
