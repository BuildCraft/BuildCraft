/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraftBuilders;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.GuiIds;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.forge.ITextureProvider;

public class BlockTemplate extends BlockContainer implements ITextureProvider {

	int blockTextureSides;
	int blockTextureFront;
	int blockTextureTopPos;
	int blockTextureTopNeg;
	
	
	public BlockTemplate(int i) {
		super(i, Material.iron);
		setHardness(0.5F);
		blockTextureSides = 3 * 16 + 0;
		blockTextureTopNeg = 3 * 16 + 1;
		blockTextureTopPos = 3 * 16 + 2;
		blockTextureFront = 3 * 16 + 4;
	}

	@Override
	public String getTextureFile() {
		return BuildCraftCore.customBuildCraftTexture;
	}

	@Override
	public TileEntity getBlockEntity() {
		return new TileTemplate();
	}
	
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {	
		if (entityplayer.getCurrentEquippedItem() != null 
				&& entityplayer.getCurrentEquippedItem().getItem() == BuildCraftCore.wrenchItem) {
			
			int meta = world.getBlockMetadata(i, j, k);

			switch (Orientations.values()[meta]) {
			case XNeg:
				world.setBlockMetadata(i, j, k, Orientations.ZPos.ordinal());
				break;
			case XPos:
				world.setBlockMetadata(i, j, k, Orientations.ZNeg.ordinal());
				break;
			case ZNeg:
				world.setBlockMetadata(i, j, k, Orientations.XNeg.ordinal());
				break;
			case ZPos:
				world.setBlockMetadata(i, j, k, Orientations.XPos.ordinal());
				break;
			}
			
			world.markBlockNeedsUpdate(i, j, k);
			
			return true;
		} else {
			TileTemplate tile = (TileTemplate) world.getBlockTileEntity(i, j, k);
			
			if(!APIProxy.isClient(world))
							entityplayer.openGui(mod_BuildCraftBuilders.instance, GuiIds.TEMPLATE, world, i, j, k);

			return true;
		}
	}	
	
	public void onBlockRemoval(World world, int i, int j, int k) {		
		Utils.preDestroyBlock(world, i, j, k);
		
		super.onBlockRemoval(world, i, j, k);
	}
	
	public void onBlockPlacedBy(World world, int i, int j, int k,
			EntityLiving entityliving) {
		super.onBlockPlacedBy(world, i, j, k, entityliving);

		Orientations orientation = Utils.get2dOrientation(new Position(
				entityliving.posX, entityliving.posY, entityliving.posZ),
				new Position(i, j, k));

		world.setBlockMetadataWithNotify(i, j, k, orientation.reverse()
				.ordinal());
	}
	
	public int getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k, int l) {
		int m = iblockaccess.getBlockMetadata(i, j, k);
					    	
    	if (l == 1) {
    		boolean isPowered = false;
    		
    		if (APIProxy.getWorld() == null) {
    			return getBlockTextureFromSideAndMetadata(l, m);
    		}
    		
			isPowered = APIProxy.getWorld()
					.isBlockIndirectlyGettingPowered(i, j, k);
    		
    		if (!isPowered) {
    			return blockTextureTopPos;
    		} else {
    			return blockTextureTopNeg;
    		}
    	}

    	return getBlockTextureFromSideAndMetadata(l, m);
	}	
	
	public int getBlockTextureFromSideAndMetadata(int i, int j) {
    	if (j == 0 && i == 3) {
			return blockTextureFront;
		}
    	
    	if (i == 1) {
    		return blockTextureTopPos;
    	}
		
		if (i == j) {
			return blockTextureFront;
		}
		
		return blockTextureSides;		
    }
}
