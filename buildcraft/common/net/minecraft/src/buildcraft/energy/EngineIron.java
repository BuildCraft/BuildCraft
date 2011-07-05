package net.minecraft.src.buildcraft.energy;

public class EngineIron extends Engine {

	public EngineIron(TileEngine engine) {
		super(engine);
		
		maxEnergy = 100000;
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
	
	public boolean isBurning () {
		return tile.burnTime > 0
				&& tile.worldObj.isBlockIndirectlyGettingPowered(tile.xCoord,
						tile.yCoord, tile.zCoord);
	}
}
