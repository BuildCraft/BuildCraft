/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.energy;

public class EngineWood extends Engine {

	public EngineWood(TileEngine engine) {
		super(engine);
		
		maxEnergy = 1000;
	}
	
	public String getTextureFile () {
		return "/net/minecraft/src/buildcraft/energy/gui/base_wood.png";
	}
	
	public int explosionRange () {
		return 1;
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
			return 0.08F;
		}
		
		return 0;
	}

	public void update () {
		super.update();
		
		if (tile.worldObj.isBlockIndirectlyGettingPowered(tile.xCoord, tile.yCoord, tile.zCoord)) {
			if ((tile.worldObj.getWorldTime() % 20) == 0) {
				energy++;
			}
		}
	}
	
	public boolean isBurning() {
		return tile.worldObj.isBlockIndirectlyGettingPowered(tile.xCoord,
				tile.yCoord, tile.zCoord);
	}

	@Override
	public int getScaledBurnTime(int i) {
		return 0;
	}

	@Override
	public void burn() {
		
	}
}
