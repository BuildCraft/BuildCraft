/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.BlockStationary;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.BuildCraftEnergy;
import net.minecraft.src.Material;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.core.ILiquid;
import net.minecraft.src.forge.ITextureProvider;

public class BlockOilStill extends BlockStationary implements ITextureProvider, ILiquid {

	public BlockOilStill(int i, Material material) {
		super(i, material);
		// TODO Auto-generated constructor stub
	}
	
	 public int getRenderType() {
		 return BuildCraftCore.oilModel;
	 }
	 
	@Override
	public String getTextureFile() {
		return BuildCraftCore.customBuildCraftTexture;
	}
	
	@Override
	public int stillLiquidId() {
		// TODO Auto-generated method stub
		return BuildCraftEnergy.oilStill.blockID;
	}
	
	@Override
    public boolean isBlockReplaceable( World world, int i, int j, int k ) {
	    return true;
    }

}
