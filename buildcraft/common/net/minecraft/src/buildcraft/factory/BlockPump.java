/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.forge.ITextureProvider;

public class BlockPump extends BlockContainer implements ITextureProvider {

	public BlockPump(int i) {
		super(i, Material.iron);
		
		
		setHardness(5F);
		// TODO Auto-generated constructor stub
	}

	@Override
	public TileEntity getBlockEntity() {	
		return new TilePump();
	}
	
	@Override
	public String getTextureFile() {		
		return BuildCraftCore.customBuildCraftTexture;
	}

	 public int getBlockTextureFromSide(int i) {
		 switch (i) {
		 case 0:
			 return 6 * 16 + 4;
		 case 1:
			 return 6 * 16 + 5;		 
		 default:
			 return 6 * 16 + 3;		 
		 }
	 }
	 
	public void onBlockRemoval(World world, int i, int j, int k) {
		Utils.preDestroyBlock(world, i, j, k);

		super.onBlockRemoval(world, i, j, k);
	}
}
