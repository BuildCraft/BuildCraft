package net.minecraft.src.buildcraft;

import net.minecraft.src.Block;
import net.minecraft.src.Entity;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraft;

public class EntityMechanicalArm extends Entity {

	double sizeX, sizeZ;
	EntityBlock xArm, yArm, zArm, head;
	
	double targetX, targetY, targetZ;
	double angle;
	
	double headPosX, headPosY, headPosZ;
	double baseY;
	double speed = 0.01;
	
	IArmListener listener;
	boolean inProgressionXZ = false;
	boolean inProgressionY = false;
	
	public EntityMechanicalArm(World world, double i, double j, double k, double width, double height) {
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
        inProgressionXZ = false;
        inProgressionY = false;

		xArm = new EntityBlock(world, i, j, k, width, 0.5, 0.5,
				mod_BuildCraft.getInstance().plainPipeBlock.blockID);
		world.entityJoinedWorld(xArm);
		
		yArm = new EntityBlock(world, i, j, k, 0.5, 1, 0.5,
				mod_BuildCraft.getInstance().plainPipeBlock.blockID);
		world.entityJoinedWorld(yArm);
		
		zArm = new EntityBlock(world, i, j, k, 0.5, 0.5, height,
				mod_BuildCraft.getInstance().plainPipeBlock.blockID);
		world.entityJoinedWorld(zArm);
		
		head = new EntityBlock(world, i, j, k, 0.2, 1, 0.2,
				Block.blockDiamond.blockID);
		world.entityJoinedWorld(head);
        		
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
		
		inProgressionXZ = true;
		inProgressionY = true;
	}
	
    public void onUpdate() {
    	super.onUpdate ();
    	
    	if (inProgressionXZ) {
			if (Math.abs(targetX - headPosX) < speed * 2
					&& Math.abs(targetZ - headPosZ) < speed * 2) {
				headPosX = targetX;
				headPosY = targetY;

				inProgressionXZ = false;
				
				if (listener != null && !inProgressionY) {
					listener.positionReached(headPosX, headPosY, headPosZ);
				}
			} else {
				headPosX += Math.cos(angle) * speed;
				headPosZ += Math.sin(angle) * speed;				
			}    		
    		updatePosition();
    	}
    	
    	if (inProgressionY) {
    		if (Math.abs(targetY - headPosY) < speed * 2) {
    			headPosY = targetY;
    			
    			inProgressionY = false;
    			
    			if (listener != null && !inProgressionXZ) {
					listener.positionReached(headPosX, headPosY, headPosZ);
				}
    		} else {
    			if (targetY > headPosY) {
    				headPosY += speed / 2;
    			} else {
    				headPosY -= speed / 2;
    			}
    			
    			updatePosition();
    		}
    			
    	}
    	
    	updatePosition();
    }
    
    public void updatePosition () {		
		xArm.setPosition(xArm.posX, xArm.posY, headPosZ + 0.25);
    	yArm.jSize = baseY - headPosY - 1;
		yArm.setPosition(headPosX + 0.25, headPosY + 1, headPosZ + 0.25);		
		zArm.setPosition(headPosX + 0.25, zArm.posY, zArm.posZ);
		
		head.setPosition(headPosX + 0.4, headPosY, headPosZ + 0.4);
    }

}
