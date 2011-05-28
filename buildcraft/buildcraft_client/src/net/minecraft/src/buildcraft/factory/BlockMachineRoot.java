package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.buildcraft.core.IMachine;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;


public abstract class BlockMachineRoot extends BlockContainer {

	protected BlockMachineRoot(int i, Material material) {
		super(i, material);
		// TODO Auto-generated constructor stub
	}
	
	public float getBlockBrightness	(IBlockAccess iblockaccess, int i, int j, int k)
    {	
		for (int x = i - 1; x <= i + 1; ++x)
			for (int y = j - 1; y <= j + 1; ++y)
				for (int z = k - 1; z <= k + 1; ++z) {
					TileEntity tile = iblockaccess.getBlockTileEntity(x, y, z);		
					
					if (tile instanceof IMachine && ((IMachine)tile).isActive()) {
						return super.getBlockBrightness(iblockaccess, i, j, k) + 0.5F;
					} 
				}
		
		return super.getBlockBrightness(iblockaccess, i, j, k);
    }
	
	
    public int getRenderType()
    {
        return BuildCraftCore.customTextureModel;
    }

	
}
