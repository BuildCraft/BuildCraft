package net.minecraft.src.buildcraft.api;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Packet;
import net.minecraft.src.World;

public class EntityPassiveItem extends Entity implements ITrackedEntity {

	public float speed = 0.01F;
	public ItemStack item;
	
	public EntityPassiveItem(World world) {
		super(world);		
		
		System.out.println ("A");
    	
    	noClip = true;    	
	}
	
	public EntityPassiveItem(World world, double d, double d1, double d2) {
		super (world);
		
		System.out.println ("B");
		
		setSize(0.25F, 0.25F);
		setPosition(d, d1, d2);
		noClip = true;
	}
	
	public EntityPassiveItem(World world, double d, double d1, double d2, 
			ItemStack itemstack) {
		this (world, d, d1, d2);
		this.item = itemstack;
    }

	@Override
	protected void entityInit() {
		// TODO Auto-generated method stub
		
	}
	
	public void onUpdate() {
		//super.onUpdate();
		
		if (APIProxy.isClient(worldObj)) {
			moveEntity(motionX, motionY, motionZ);
		}
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

	@Override
	public Packet getSpawnPacket() {
		return new Packet121PassiveItemSpawn(this);
	}

	@Override
	public Packet getUpdatePacket() {		
		return new Packet122PassiveItemUpdate(this);
	}

	@Override
	public int getUpdateFrequency() {
		return 5;
	}

	@Override
	public int getMaximumDistance() {
		return 64;
	}

}
