package net.minecraft.src.buildcraft;

import net.minecraft.src.Block;
import net.minecraft.src.Entity;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

public class EntityMechanicalArm extends Entity {

	int sizeX, sizeZ;
	EntityBlock xArm, yArm, zArm, head;
	
	double targetX, targetY, targetZ;
	double angle;
	
	double headPosX, headPosY, headPosZ;
	double baseY;
	double speed = 0.01;
	
	IArmListener listener;
	boolean inProgression = false;	
	
	public EntityMechanicalArm(World world, double i, double j, double k, int width, int height) {
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
        noClip = true;
        baseY = j;
        
        headPosX = i;
        headPosY = j - 2;
        headPosZ = k;

        setTarget (headPosX, headPosY, headPosZ);
        inProgression = false;

		xArm = new EntityBlock(world, i, j, k, width, 0.5, 0.5,
				Block.cobblestone.blockID);
		world.entityJoinedWorld(xArm);
		
		yArm = new EntityBlock(world, i, j, k, 0.5, 1, 0.5,
				Block.sand.blockID);
		world.entityJoinedWorld(yArm);
		
		zArm = new EntityBlock(world, i, j, k, 0.5, 0.5, height,
				Block.brick.blockID);
		world.entityJoinedWorld(zArm);
		
		updatePosition();
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
	
	public void setTarget (double x, double y, double z) {
		targetX = x;
		targetY = y;
		targetZ = z;
		
		double dX = targetX - headPosX;
		double dZ = targetZ - headPosZ;
		
		angle = Math.atan2(dZ, dX);
		
		inProgression = true;
	}
	
    public void onUpdate() {
    	super.onUpdate ();
    	
    	if (inProgression) {
			System.out.println(Math.abs(targetX - headPosX) + ", "
					+ Math.abs(targetZ - headPosZ));
			if (Math.abs(targetX - headPosX) < speed * 2
					&& Math.abs(targetZ - headPosZ) < speed * 2) {
				headPosX = targetX;
				headPosY = targetY;

				inProgression = false;

				if (listener != null) {
					listener.positionReached(headPosX, headPosY, headPosZ);
				}
			} else {
				headPosX += Math.cos(angle) * speed;
				headPosZ += Math.sin(angle) * speed;				
			}
    		
    		updatePosition();
    	}
    }
    
    public void updatePosition () {
		xArm.setPosition(xArm.posX, xArm.posY, headPosZ);
    	yArm.jSize = baseY - headPosY;
		yArm.setPosition(headPosX, headPosY, headPosZ);
		zArm.setPosition(headPosX, zArm.posY, zArm.posZ);
    }

}
