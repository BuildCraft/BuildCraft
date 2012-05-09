/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.BlockStationary;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.BuildCraftEnergy;
import net.minecraft.src.Material;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.liquids.ILiquid;
import net.minecraft.src.forge.ITextureProvider;

public class BlockOilStill extends BlockStationary implements ITextureProvider, ILiquid {

	public BlockOilStill(int i, Material material) {
		super(i, material);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	 public int getRenderType() {
		 return BuildCraftCore.oilModel;
	 }
	 
	@Override
	public String getTextureFile() {
		return BuildCraftCore.customBuildCraftTexture;
	}
	
	@Override public int stillLiquidId() { return BuildCraftEnergy.oilStill.blockID; }
	@Override public boolean isMetaSensitive() { return false; }
	@Override public int stillLiquidMeta() { return 0; }
	
	@Override
    public boolean isBlockReplaceable( World world, int i, int j, int k ) {
	    return true;
    }

}
