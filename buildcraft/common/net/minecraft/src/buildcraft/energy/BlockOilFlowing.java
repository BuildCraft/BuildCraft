package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.BlockFlowing;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.Material;
import net.minecraft.src.buildcraft.core.ICustomTextureBlock;

public class BlockOilFlowing extends BlockFlowing implements ICustomTextureBlock {

	public BlockOilFlowing(int i, Material material) {
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
