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

public class BlockBuilder extends BlockContainer implements ITextureProvider {

	int blockTextureTop;
	int blockTextureSide;
	int blockTextureFront;
	
	public BlockBuilder(int i) {
		super(i, Material.iron);
		blockTextureSide = 3 * 16 + 5;
		blockTextureTop = 3 * 16 + 6;
		blockTextureFront = 3 * 16 + 7;
		setHardness(0.7F);
	}

	@Override
	public TileEntity getBlockEntity() {
		return new TileBuilder();
	}

	@Override
	public String getTextureFile() {
		return BuildCraftCore.customBuildCraftTexture;
	}

    public int getBlockTextureFromSideAndMetadata(int i, int j) {
    	if (j == 0 && i == 3) {
			return blockTextureFront;
		}
		
		if (i == j) {
			return blockTextureFront;
		}

		switch (i) {
		case 1:
			return blockTextureTop;
		default:
			return blockTextureSide;
		}
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
			if(!APIProxy.isClient(world))
				entityplayer.openGui(mod_BuildCraftBuilders.instance, GuiIds.BUILDER, world, i, j, k);
			return true;
		}
	}
	
    public void onBlockPlacedBy(World world, int i, int j, int k, EntityLiving entityliving) {
    	super.onBlockPlacedBy(world, i, j, k, entityliving);
    	
		Orientations orientation = Utils.get2dOrientation(new Position(
				entityliving.posX, entityliving.posY, entityliving.posZ),
				new Position(i, j, k));
    	
		world.setBlockMetadataWithNotify(i, j, k, orientation.reverse()
				.ordinal());
    }
    

    public void onBlockRemoval(World world, int i, int j, int k) {
    	Utils.preDestroyBlock(world, i, j, k);
    	super.onBlockRemoval(world, i, j, k);
    }

}
