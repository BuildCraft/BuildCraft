/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
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

	@Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }

	@Override
    public boolean isOpaqueCube() {
        return false;
    }
   
	@Override
	public TileEntity getBlockEntity() {
		return new TilePollution();
	}
	
	@SuppressWarnings({ "all" })
	public int getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k, int l) {
		return 5 * 16 + iblockaccess.getBlockMetadata(i, j, k); 
	}	
}
