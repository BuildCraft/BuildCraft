/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.builders;

import java.util.ArrayList;

import net.minecraft.src.IBlockAccess;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.core.Utils;

public class BlockPathMarker extends BlockMarker {

	public BlockPathMarker(int i) {
		super(i);		
		
		blockIndexInTexture = 3 * 16 + 10;
	}
	
	@Override
	public TileEntity getBlockEntity() {
		return new TilePathMarker();
	}
    
    @Override
	public void onBlockRemoval(World world, int i, int j, int k) {
    	Utils.preDestroyBlock(world, i, j, k);
    	
        super.onBlockRemoval(world, i, j, k);       
    }    
    
    @SuppressWarnings({ "all" })
    // @Override (client only)
	public int getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k, int l) {
		TilePathMarker marker = (TilePathMarker) iblockaccess.getBlockTileEntity(i, j, k);
		
		if (l == 1 || (marker != null && marker.currentWorldIterator != null)) {
			return 3 * 16 + 11;
		} else {
			return 3 * 16 + 10;
		}
	}
    
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}
}
