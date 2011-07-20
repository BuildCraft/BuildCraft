package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.BlockStationary;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.Material;
import net.minecraft.src.forge.ITextureProvider;

public class BlockOilStill extends BlockStationary implements ITextureProvider {

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
}
