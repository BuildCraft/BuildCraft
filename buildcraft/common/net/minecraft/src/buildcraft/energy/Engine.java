package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.buildcraft.api.Orientations;

public abstract class Engine {

	private TileEngine tile;
	
	enum EnergyStage {
		Blue,
		Green,
		Yellow,
		Red,
		Explosion
	}
	
	public Engine (TileEngine tile) {
		this.tile = tile;
	}
	
	public float progress;
	public Orientations orientation;
	int energy;	
		
	public abstract EnergyStage getEnergyStage ();
	
	public abstract String getTextureFile ();
	
	public abstract int explosionRange ();
	
	public abstract int maxEnergyReceived ();
	
	public abstract float getPistonSpeed ();

	public void addEnergy (int addition) {
		energy += addition;
		
		if (getEnergyStage() == EnergyStage.Explosion) {
			tile.worldObj.createExplosion(null, tile.xCoord, tile.yCoord,
					tile.zCoord, explosionRange());
		}
	}
}
