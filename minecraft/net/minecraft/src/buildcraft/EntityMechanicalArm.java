package net.minecraft.src.buildcraft;

import net.minecraft.src.Block;
import net.minecraft.src.Entity;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

public class EntityMechanicalArm extends Entity {

	int sizeX, sizeZ;
	EntityBlock xArm, yArm, zArm, head;
	
	public EntityMechanicalArm(World world, int i, int j, int k, int width, int height) {
		super(world);
		
		setPosition(i, j, k);
        motionX = 0.0D;
        motionY = 0.0D;
        motionZ = 0.0D;
        prevPosX = i;
        prevPosY = j;
        prevPosZ = k;
        sizeX = height;
        sizeZ = width;
        yOffset = height / 2.0F;
        noClip = true;

		xArm = new EntityBlock(world, i, j + 1.0, k, 2.0, 0.5, 0.5,
				Block.cobblestone.blockID);
		xArm.setVelocity(0.0, 0.0, 0.05);
		world.entityJoinedWorld(xArm);
		
		zArm = new EntityBlock(world, i, j + 1.0, k, 0.5, 0.5, height,
				Block.cobblestone.blockID);
		zArm.setVelocity(0.03, 0.0, 0.0);
		world.entityJoinedWorld(zArm);
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
	
    public void onUpdate() {
    	super.onUpdate ();
    	
    	if (xArm.posZ > posZ + sizeZ) {
    		xArm.setVelocity(0.0, 0.0, -0.05);
    	} else if (xArm.posZ < posZ) {
    		xArm.setVelocity(0.0, 0.0, 0.05);
    	}
    	
    	if (zArm.posX > posX + sizeX) {
    		zArm.setVelocity(-0.03, 0.0, 0.0);
    	} else if (zArm.posX < posX) {
    		zArm.setVelocity(+0.03, 0.0, 0.0);
    	}
    	
    	xArm.moveEntity(xArm.motionX, xArm.motionY, xArm.motionZ);
    	zArm.moveEntity(zArm.motionX, zArm.motionY, zArm.motionZ);
    }

}
