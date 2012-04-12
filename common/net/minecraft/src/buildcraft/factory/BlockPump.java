/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.factory;

import java.util.ArrayList;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.forge.ITextureProvider;

public class BlockPump extends BlockContainer implements ITextureProvider {

	@Override
	public void addCreativeItems(ArrayList a) {
		a.add(new ItemStack(this, 1));
	}
	
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
