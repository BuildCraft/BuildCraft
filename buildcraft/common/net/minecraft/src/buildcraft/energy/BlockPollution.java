/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;

import net.minecraft.src.forge.ITextureProvider;

public class BlockPollution extends BlockContainer implements ITextureProvider {

	public BlockPollution(int i) {
		super(i, Material.air);	
		blockIndexInTexture = 5 * 16 + 0;
	}

	@Override
	public String getTextureFile() {
		return BuildCraftCore.customBuildCraftTexture;
	}

    public boolean renderAsNormalBlock()
    {
        return false;
    }

    public boolean isOpaqueCube() {
        return false;
    }
   
	@Override
	public TileEntity getBlockEntity() {
		return new TilePollution();
	}
	
	public int getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k, int l) {
		return 5 * 16 + iblockaccess.getBlockMetadata(i, j, k); 
	}	
}
