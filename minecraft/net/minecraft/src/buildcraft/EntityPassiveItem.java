package net.minecraft.src.buildcraft;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

public class EntityPassiveItem extends EntityItem {

	public EntityPassiveItem(World world) {
		super(world);
		// TODO Auto-generated constructor stub
	}
	
    public EntityPassiveItem(World world, double d, double d1, double d2, 
            ItemStack itemstack) {
    	super (world, d, d1, d2, itemstack);
    	
    	field_804_d = 0;
    }

	@Override
	protected void entityInit() {
		// TODO Auto-generated method stub
		
	}
	
	public void onUpdate() {		
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		// TODO Auto-generated method stub
		
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

}
