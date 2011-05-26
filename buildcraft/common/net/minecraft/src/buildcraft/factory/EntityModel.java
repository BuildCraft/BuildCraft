package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.Entity;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

public class EntityModel extends Entity {

	public EntityModel (World world, double i, double j, double k) {
		super(world);
		// TODO Auto-generated constructor stub
		
        motionX = 0.0D;
        motionY = 0.0D;
        motionZ = 0.0D;
        prevPosX = i;
        prevPosY = j;
        prevPosZ = k;
        setPosition(i, j, k);
//        this.blockID = blockID;
//        this.iSize = iSize;
//        this.jSize = jSize;
//        this.kSize = kSize;   
	}

	@Override
	protected void entityInit() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		// TODO Auto-generated method stub

	}

}
