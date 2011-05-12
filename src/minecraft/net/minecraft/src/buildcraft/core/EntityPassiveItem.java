package net.minecraft.src.buildcraft.core;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

public class EntityPassiveItem extends EntityItem {

	public float speed = 0.01F;
	
	public EntityPassiveItem(World world) {
		super(world);
		
    	field_804_d = 0;
    	noClip = true;
	}
	
    public EntityPassiveItem(World world, double d, double d1, double d2, 
            ItemStack itemstack) {
    	super (world, d, d1, d2, itemstack);
    	
    	field_804_d = 0;
    	noClip = true;
    }

	@Override
	protected void entityInit() {
		// TODO Auto-generated method stub
		
	}
	
	public void onUpdate() {		
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);
		posX = nbttagcompound.getDouble("x");
		posY = nbttagcompound.getDouble("y");
		posZ = nbttagcompound.getDouble("z");		
		speed = nbttagcompound.getFloat("speed");
		
		setPosition (posX, posY, posZ);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setDouble("x", posX);
		nbttagcompound.setDouble("y", posY);
		nbttagcompound.setDouble("z", posZ);
		nbttagcompound.setFloat("speed", speed);
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
							
		EntityItem entityitem = new EntityItem(world, posX, posY, posZ, item);

		float f3 = 0.00F + world.rand.nextFloat() * 0.04F - 0.02F;
		entityitem.motionX = (float) world.rand.nextGaussian() * f3 + motion.i;
		entityitem.motionY = (float) world.rand.nextGaussian() * f3
				+ + motion.j;
		entityitem.motionZ = (float) world.rand.nextGaussian() * f3 + + motion.k;
		world.entityJoinedWorld(entityitem);
		
		return entityitem;
	}

}
