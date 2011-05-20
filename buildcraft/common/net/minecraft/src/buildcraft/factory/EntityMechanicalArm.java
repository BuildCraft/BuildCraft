package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.Block;
import net.minecraft.src.Entity;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraftFactory;
import net.minecraft.src.buildcraft.core.EntityBlock;

public class EntityMechanicalArm extends Entity {

	double sizeX, sizeZ;
	EntityBlock xArm, yArm, zArm, head;
	
	double targetX, targetY, targetZ;
	double angle;
	
	double headPosX, headPosY, headPosZ;
	double baseY;
	double speed = 0.03;
	
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

		xArm = new EntityBlock(world, i, j, k, width, 0.5, 0.5,
				mod_BuildCraftFactory.plainPipeBlock.blockID);
		xArm.texture = mod_BuildCraftFactory.drillTexture;
		world.entityJoinedWorld(xArm);
		
		yArm = new EntityBlock(world, i, j, k, 0.5, 1, 0.5,
				mod_BuildCraftFactory.plainPipeBlock.blockID);
		yArm.texture = mod_BuildCraftFactory.drillTexture;
		world.entityJoinedWorld(yArm);
		
		zArm = new EntityBlock(world, i, j, k, 0.5, 0.5, height,
				mod_BuildCraftFactory.plainPipeBlock.blockID);
		zArm.texture = mod_BuildCraftFactory.drillTexture;
		world.entityJoinedWorld(zArm);
		
		head = new EntityBlock(world, i, j, k, 0.2, 1, 0.2,
				Block.blockDiamond.blockID);		
		world.entityJoinedWorld(head);
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
		
		xArm.texture = mod_BuildCraftFactory.drillTexture;
		yArm.texture = mod_BuildCraftFactory.drillTexture;
		zArm.texture = mod_BuildCraftFactory.drillTexture;
		
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
	
    public void onUpdate() {
    	super.onUpdate ();
    	
    	if (inProgressionXZ) {
			if (Math.abs(targetX - headPosX) < speed * 2
					&& Math.abs(targetZ - headPosZ) < speed * 2) {
				headPosX = targetX;
				headPosZ = targetZ;

				inProgressionXZ = false;
				
				if (listener != null && !inProgressionY) {
					listener.positionReached(this);
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
					listener.positionReached(this);
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

    public void joinToWorld (World w) {
    	super.worldObj = w;    		    		

    	xArm.worldObj = w;
    	yArm.worldObj = w;
    	zArm.worldObj = w;
    	head.worldObj = w;

    	w.entityJoinedWorld(this);
    	w.entityJoinedWorld(xArm);
    	w.entityJoinedWorld(yArm);
    	w.entityJoinedWorld(zArm);
    	w.entityJoinedWorld(head);
    }

	public void setEntityDead() {
		xArm.setEntityDead ();
		yArm.setEntityDead ();
		zArm.setEntityDead ();
		head.setEntityDead ();
		super.setEntityDead ();
		
	}
}
