/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.BuildCraftFactory;
import net.minecraft.src.Entity;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.core.EntityBlock;

public class EntityMechanicalArm extends Entity {

	double sizeX, sizeZ;
	EntityBlock xArm, yArm, zArm, head;
		
	double angle;
	
	public double targetX, targetY, targetZ;
	public double headPosX, headPosY, headPosZ;
	public double speed = 0.03;
	
	double baseY;
		
	IArmListener listener;
	boolean inProgressionXZ = false;
	boolean inProgressionY = false;
	
	public EntityMechanicalArm(World world) {
		super (world);
	}
	
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

		xArm = new EntityBlock(world, i, j, k, width, 0.5, 0.5);
		xArm.texture = BuildCraftFactory.drillTexture;
		world.spawnEntityInWorld(xArm);
		
		yArm = new EntityBlock(world, i, j, k, 0.5, 1, 0.5);
		yArm.texture = BuildCraftFactory.drillTexture;
		world.spawnEntityInWorld(yArm);
		
		zArm = new EntityBlock(world, i, j, k, 0.5, 0.5, height);
		zArm.texture = BuildCraftFactory.drillTexture;
		world.spawnEntityInWorld(zArm);
		
		head = new EntityBlock(world, i, j, k, 0.2, 1, 0.2);
		head.texture = 2 * 16 + 10;
		world.spawnEntityInWorld(head);
		head.shadowSize = 1.0F;
        		
		updatePosition();
	}

	@Override
	protected void entityInit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		sizeX = nbttagcompound.getDouble("sizeX");
		sizeZ = nbttagcompound.getDouble("sizeZ");
		
		targetX = nbttagcompound.getDouble("targetX");
		targetY = nbttagcompound.getDouble("targetY");
		targetZ = nbttagcompound.getDouble("targetZ");
		angle = nbttagcompound.getDouble("angle");
		
		headPosX = nbttagcompound.getDouble("headPosX");
		headPosY = nbttagcompound.getDouble("headPosY");
		headPosZ = nbttagcompound.getDouble("headPosZ");

		baseY = nbttagcompound.getDouble("baseY");
		speed = nbttagcompound.getDouble("speed");
		
		inProgressionXZ = nbttagcompound.getBoolean("progressionXY");
		inProgressionY = nbttagcompound.getBoolean("progressionY");
		
		NBTTagCompound xArmStore, yArmStore, zArmStore, headStore;
		
		xArmStore = nbttagcompound.getCompoundTag("xArm");
		yArmStore = nbttagcompound.getCompoundTag("yArm");
		zArmStore = nbttagcompound.getCompoundTag("zArm");
		headStore = nbttagcompound.getCompoundTag("head");
		
		xArm = new EntityBlock(worldObj);
		yArm = new EntityBlock(worldObj);
		zArm = new EntityBlock(worldObj);
		head = new EntityBlock(worldObj);
		
		xArm.texture = BuildCraftFactory.drillTexture;
		yArm.texture = BuildCraftFactory.drillTexture;
		zArm.texture = BuildCraftFactory.drillTexture;
		head.texture = 2 * 16 + 10;
		
		xArm.readFromNBT(xArmStore);
		yArm.readFromNBT(yArmStore);
		zArm.readFromNBT(zArmStore);
		head.readFromNBT(headStore);			
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setDouble("sizeX", sizeX);
		nbttagcompound.setDouble("sizeZ", sizeZ);
		
		nbttagcompound.setDouble("targetX", targetX);
		nbttagcompound.setDouble("targetY", targetY);
		nbttagcompound.setDouble("targetZ", targetZ);
		nbttagcompound.setDouble("angle", angle);
		
		nbttagcompound.setDouble("headPosX", headPosX);
		nbttagcompound.setDouble("headPosY", headPosY);
		nbttagcompound.setDouble("headPosZ", headPosZ);

		nbttagcompound.setDouble("baseY", baseY);
		nbttagcompound.setDouble("speed", speed);
		
		nbttagcompound.setBoolean("progressionXY", inProgressionXZ);
		nbttagcompound.setBoolean("progressionY", inProgressionY);
		
		NBTTagCompound xArmStore, yArmStore, zArmStore, headStore;
		
		xArmStore = new NBTTagCompound();
		yArmStore = new NBTTagCompound();
		zArmStore = new NBTTagCompound();
		headStore = new NBTTagCompound();
		
		nbttagcompound.setTag("xArm", xArmStore);
		nbttagcompound.setTag("yArm", yArmStore);
		nbttagcompound.setTag("zArm", zArmStore);
		nbttagcompound.setTag("head", headStore);
		
		xArm.writeToNBT(xArmStore);
		yArm.writeToNBT(yArmStore);
		zArm.writeToNBT(zArmStore);
		head.writeToNBT(headStore);			
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
	
	public double [] getTarget () {
		return new double [] {targetX, targetY, targetZ};
	}
	
	@Override
	public void onUpdate() {
		if (speed > 0) {
			doMove(speed);
		}
	}
	
    public void doMove(double instantSpeed) {
    	super.onUpdate ();
    	
    	if (inProgressionXZ) {
			if (Math.abs(targetX - headPosX) < instantSpeed * 2
					&& Math.abs(targetZ - headPosZ) < instantSpeed * 2) {
				headPosX = targetX;
				headPosZ = targetZ;

				inProgressionXZ = false;
				
				if (listener != null && !inProgressionY) {
					listener.positionReached(this);
				}
			} else {
				headPosX += Math.cos(angle) * instantSpeed;
				headPosZ += Math.sin(angle) * instantSpeed;				
			}
    	}
    	
    	if (inProgressionY) {
    		if (Math.abs(targetY - headPosY) < instantSpeed * 2) {
    			headPosY = targetY;
    			
    			inProgressionY = false;
    			
    			if (listener != null && !inProgressionXZ) {
					listener.positionReached(this);
				}
    		} else {
    			if (targetY > headPosY) {
    				headPosY += instantSpeed / 2;
    			} else {
    				headPosY -= instantSpeed / 2;
    			}
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

    public void joinToWorld (World w) {
    	super.worldObj = w;    		    		

    	xArm.worldObj = w;
    	yArm.worldObj = w;
    	zArm.worldObj = w;
    	head.worldObj = w;

    	w.spawnEntityInWorld(this);
    	w.spawnEntityInWorld(xArm);
    	w.spawnEntityInWorld(yArm);
    	w.spawnEntityInWorld(zArm);
    	w.spawnEntityInWorld(head);
    }

	public void setEntityDead() {
		xArm.setDead ();
		yArm.setDead ();
		zArm.setDead ();
		head.setDead ();
		super.setDead ();
		
	}
	
	public double [] getHeadPosition () {
		return new double [] {headPosX, headPosY, headPosZ};
	}
	
	public void setHeadPosition (double x, double y, double z) {
		headPosX = x;
		headPosY = y;
		headPosZ = z;
	}
}
