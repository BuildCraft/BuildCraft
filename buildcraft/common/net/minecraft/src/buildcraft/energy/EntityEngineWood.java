package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.World;

public class EntityEngineWood extends EntityEngine {
	
	public EntityEngineWood(World world) {
		super(world);
	}

	public EnergyStage getEnergyStage () {
		if (energy <= 25) {
			return EnergyStage.Blue;
		} else if (energy <= 50) {
			return EnergyStage.Green;
		} else if (energy <= 75) {
			return EnergyStage.Yellow;
		} else if (energy <= 100) {
			return EnergyStage.Red;
		} else {
			return EnergyStage.Explosion;
		}
	}
	
	public String getTextureFile () {
		return "/net/minecraft/src/buildcraft/energy/gui/base_wood.png";
	}
	
	public int explosionRange () {
		return 2;
	}
	
	public int maxEnergyReceived () {
		return 50;
	}
	
	public float getPistonSpeed () {
		switch (getEnergyStage()) {
		case Blue:
			return 0.01F;
		case Green:
			return 0.02F;
		case Yellow:
			return 0.04F;
		case Red:
			return 0.8F;
		}
		
		return 0;
	}

}
