package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.World;

public class EngineStone extends Engine {

	public EngineStone(TileEngine engine) {
		super(engine);
		// TODO Auto-generated constructor stub
	}

	public EnergyStage getEnergyStage () {
		if (energy <= 250) {
			return EnergyStage.Blue;
		} else if (energy <= 500) {
			return EnergyStage.Green;
		} else if (energy <= 750) {
			return EnergyStage.Yellow;
		} else if (energy <= 1000) {
			return EnergyStage.Red;
		} else {
			return EnergyStage.Explosion;
		}
	}
	
	public String getTextureFile () {
		return "/net/minecraft/src/buildcraft/energy/gui/base_stone.png";
	}
	
	public int explosionRange () {
		return 4;
	}
	
	public int maxEnergyReceived () {
		return 200;
	}
	
	public float getPistonSpeed () {
		switch (getEnergyStage()) {
		case Blue:
			return 0.02F;
		case Green:
			return 0.04F;
		case Yellow:
			return 0.08F;
		case Red:
			return 0.16F;
		}
		
		return 0;
	}

}
