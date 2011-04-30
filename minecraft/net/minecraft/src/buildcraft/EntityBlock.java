package net.minecraft.src.buildcraft;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Entity;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

public class EntityBlock extends Entity {

	public int blockID;
	
	public double iSize, jSize, kSize;
	
	public EntityBlock(World world) {
		super(world);
//		setSize((float) iSize, (float) jSize);
		
		preventEntitySpawning = true;      
		noClip = true;
	}
	
    public EntityBlock (World world, double i, double j, double k, double iSize, double jSize, double kSize, int blockID) {
    	this (world);
        
        motionX = 0.0D;
        motionY = 0.0D;
        motionZ = 0.0D;
        prevPosX = i;
        prevPosY = j;
        prevPosZ = k;
        this.blockID = blockID;
        this.iSize = iSize;
        this.jSize = jSize;
        this.kSize = kSize;    
        
        setPosition(i, j, k);
    }
    
    @Override
    public void setPosition(double d, double d1, double d2) {
    	posX = d;
    	posY = d1;
    	posZ = d2;
    	
        boundingBox.minX = posX;
        boundingBox.minY = posY;
        boundingBox.minZ = posZ;
        
        boundingBox.maxX = posX + iSize;
        boundingBox.maxY = posY + jSize;
        boundingBox.maxZ = posZ + kSize;
    }
    
    @Override
    public void moveEntity(double d, double d1, double d2) {
    	setPosition (posX + d, posY + d1, posZ + d2);
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

    public boolean canBeCollidedWith()
    {
        return !isDead;
    }
}
