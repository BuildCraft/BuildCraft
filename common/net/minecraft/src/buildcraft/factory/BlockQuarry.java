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

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.BuildCraftFactory;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.forge.ITextureProvider;

public class BlockQuarry extends BlockMachineRoot implements
		ITextureProvider {
	
	int textureTop;
	int textureFront;
	int textureSide;
	
	public BlockQuarry(int i) {
		super(i, Material.iron);
		
		setHardness(1.5F);
		setResistance(10F);
		setStepSound(soundStoneFootstep);
		
		textureSide = 2 * 16 + 9;
		textureFront = 2 * 16 + 7;
		textureTop = 2 * 16 + 8;	
		
	}
    
	@Override
    public void onBlockPlacedBy(World world, int i, int j, int k, EntityLiving entityliving) {
    	super.onBlockPlacedBy(world, i, j, k, entityliving);
    	
		Orientations orientation = Utils.get2dOrientation(new Position(
				entityliving.posX, entityliving.posY, entityliving.posZ),
				new Position(i, j, k));
    	
		world.setBlockMetadataWithNotify(i, j, k, orientation.reverse()
				.ordinal());
    }

	@Override
	public int getBlockTextureFromSideAndMetadata(int i, int j) {
		// If no metadata is set, then this is an icon.
		if (j == 0 && i == 3) {
			return textureFront;
		}
		
		if (i == j) {
			return textureFront;
		}

		switch (i) {
		case 1:
			return textureTop;
		default:
			return textureSide;
		}
	}
    
	@Override
	public TileEntity getBlockEntity() {		
		return new TileQuarry();
	}
	
	public void searchFrames(World world, int i, int j, int k) {
		int width2 = 1;
		if (!world.checkChunksExist(i - width2, j - width2, k - width2, i + width2, j + width2, k + width2))
			return;

		int blockID = world.getBlockId(i, j, k);

		if (blockID != BuildCraftFactory.frameBlock.blockID)
			return;

		int meta = world.getBlockMetadata(i, j, k);

		if ((meta & 8) == 0) {
			world.setBlockMetadata(i, j, k, meta | 8);

			Orientations[] dirs = Orientations.dirs();

			for (Orientations dir : dirs) {
				switch (dir) {
				case YPos:
					searchFrames(world, i, j + 1, k);
				case YNeg:
					searchFrames(world, i, j - 1, k);
				case ZPos:
					searchFrames(world, i, j, k + 1);
				case ZNeg:
					searchFrames(world, i, j, k - 1);
				case XPos:
					searchFrames(world, i + 1, j, k);
				case XNeg:
					searchFrames(world, i - 1, j, k);
				}
			}

		}
	}
	
	@Override
	public void onBlockRemoval(World world, int i, int j, int k) {
		Utils.preDestroyBlock(world, i, j, k);

		byte width = 1;
		int width2 = width + 1;

		if(world.checkChunksExist(i - width2, j - width2, k - width2, i + width2, j + width2, k + width2)) {

			boolean frameFound = false;
			for (int z = -width; z <= width; ++z) {
				
				for (int y = -width; y <= width; ++y) {
					
					for (int x = -width; x <= width; ++x) {
						
						int blockID = world.getBlockId(i + z, j + y, k + x);

						if (blockID == BuildCraftFactory.frameBlock.blockID) {
							searchFrames(world, i + z, j + y, k + x);
							frameFound = true;
							break;
						}
					}
					if (frameFound)
						break;
				}
				if (frameFound)
					break;
			}
		}

		super.onBlockRemoval(world, i, j, k);
	}
	
	@Override
	public String getTextureFile() {	
		return BuildCraftCore.customBuildCraftTexture;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}
}
