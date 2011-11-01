/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.Material;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import net.minecraft.src.TileEntity;
import net.minecraft.src.EntityLiving;

import net.minecraft.src.buildcraft.api.ILiquidContainer;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.IPipeConnection;
import net.minecraft.src.buildcraft.api.ISpecialInventory;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.forge.ITextureProvider;


public class BlockDockingStation extends BlockContainer implements 
			ITextureProvider, IPipeConnection {
			
	int textureSide;
	int textureTopN;
	int textureTopS;
	int textureTopW;
	int textureTopE;
	int textureBottom;
			
			
	public BlockDockingStation(int i) {
		super(i, Material.iron);
		
		textureSide = 37;
		textureBottom = 38;
		textureTopN = blockIndexInTexture = 128;
		textureTopS = 129;
		textureTopE = 130;
		textureTopW = 131;
		
		setHardness(0.5F);
	}
			
	@Override
	public String getTextureFile() {
		return BuildCraftCore.customBuildCraftTexture;
	}
	
    public void onBlockPlacedBy(World world, int i, int j, int k, EntityLiving entityliving) {
    	super.onBlockPlacedBy(world, i, j, k, entityliving);
    	
		Orientations orientation = Utils.get2dOrientation(new Position(
				entityliving.posX, entityliving.posY, entityliving.posZ),
				new Position(i, j, k));
    	
		world.setBlockMetadataWithNotify(i, j, k, orientation.reverse()
				.ordinal());
				
		System.out.println("ORIENTATION");
		System.out.println(orientation.reverse().ordinal());
    }
	
	@Override
	public int getBlockTextureFromSideAndMetadata(int i, int j) {
		switch(i) {
		case 0:
			return textureBottom;
		case 1:
			switch(j) {
				case 0: //icon
					return textureTopE;
				case 2:
					return textureTopS;
				case 3:
					return textureTopN;
				case 4:
					return textureTopE;
				case 5:
					return textureTopW;
				default:
					return textureTopN;
			}
		default:
			return textureSide;
		}
	}
	
	@Override
	public boolean isPipeConnected(IBlockAccess blockAccess, int x1, int y1,
			int z1, int x2, int y2, int z2) {
		return true;
	}
	
	public TileEntity getBlockEntity() {
		return new TileDockingStation();
	}
	
}