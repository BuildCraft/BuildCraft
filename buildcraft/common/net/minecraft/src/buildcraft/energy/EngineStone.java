package net.minecraft.src.buildcraft.energy;

public class EngineStone extends Engine {

	public EngineStone(TileEngine engine) {
		super(engine);
		
		maxEnergy = 10000;
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
	
	public boolean isBurning () {
		return tile.burnTime > 0;
	}

}
