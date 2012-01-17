/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.silicon;

import java.util.ArrayList;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.BuildCraftSilicon;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.forge.ITextureProvider;

public class BlockLaser extends BlockContainer implements ITextureProvider {

	public BlockLaser(int i) {
		super(i, Material.iron);
		setHardness(0.5F);
	}
	
	@Override
	public int getRenderType() {
		return BuildCraftSilicon.laserBlockModel;
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	public boolean isACube() {
		return false;
	}


	@Override
	public TileEntity getBlockEntity() {
		return new TileLaser();
	}

	@Override
	public String getTextureFile() {
		return BuildCraftCore.customBuildCraftTexture;
	}
	
	@Override
	public int getBlockTextureFromSideAndMetadata(int i, int j) {
		if (i == Orientations.values()[j].reverse().ordinal()) {
			return 16 * 2 + 15;	
		} else if (i == j) {
			return 16 * 2 + 14;	
		} else {
			return 16 * 2 + 13;
		}
		
	}
	
	@Override
	public void onBlockPlaced(World world, int i, int j, int k, int l) {
    	super.onBlockPlaced(world, i, j, k, l);
        int i1 = world.getBlockMetadata(i, j, k);
        if (l <= 6) {
        	i1 = l;
        }
        world.setBlockMetadataWithNotify(i, j, k, i1);
    }

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}
}
