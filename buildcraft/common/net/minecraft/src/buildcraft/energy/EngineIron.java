package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.World;

public class EngineIron extends Engine {


	public EngineIron(TileEngine engine) {
		super(engine);
		// TODO Auto-generated constructor stub
	}

	public EnergyStage getEnergyStage () {
		if (energy <= 2500) {
			return EnergyStage.Blue;
		} else if (energy <= 5000) {
			return EnergyStage.Green;
		} else if (energy <= 7500) {
			return EnergyStage.Yellow;
		} else if (energy <= 10000) {
			return EnergyStage.Red;
		} else {
			return EnergyStage.Explosion;
		}
	}
	
	public String getTextureFile () {
		return "/net/minecraft/src/buildcraft/energy/gui/base_iron.png";
	}
	
	public int explosionRange () {
		return 8;
	}
	
	public int maxEnergyReceived () {
		return 2000;
	}

	public float getPistonSpeed () {
		switch (getEnergyStage()) {
		case Blue:
			return 0.04F;
		case Green:
			return 0.08F;
		case Yellow:
			return 0.16F;
		case Red:
			return 0.32F;
		}
		
		return 0;
	}
}
