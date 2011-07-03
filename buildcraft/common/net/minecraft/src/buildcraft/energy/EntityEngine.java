package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.Entity;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.Orientations;

public abstract class EntityEngine extends Entity {

	enum EnergyStage {
		Blue,
		Green,
		Yellow,
		Red,
		Explosion
	}
	
	public float progress;
	public Orientations orientation;
	int energy;	
	
	public EntityEngine(World world) {
		super(world);
		setSize(2F, 0.99F);
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
	
	public abstract EnergyStage getEnergyStage ();
	
	public abstract String getTextureFile ();
	
	public abstract int explosionRange ();
	
	public abstract int maxEnergyReceived ();
	
	public abstract float getPistonSpeed ();

	public void addEnergy (int addition) {
		energy += addition;
		
		if (getEnergyStage() == EnergyStage.Explosion) {
			worldObj.createExplosion(null, posX, posY, posZ, explosionRange());
		}
	}
}
