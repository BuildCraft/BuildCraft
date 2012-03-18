/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
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
	
	public void delete() {
	
	}

	@Override
	public void burn() {
		
	}
}
