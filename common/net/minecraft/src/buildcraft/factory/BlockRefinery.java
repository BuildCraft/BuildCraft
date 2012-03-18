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
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.BuildCraftAPI;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.Utils;

public class BlockRefinery extends BlockContainer {

	public BlockRefinery(int i) {
		super(i, Material.iron);
		
		setHardness(0.5F);
	}
	
	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	public boolean isACube () {
		return false;
	}

	@Override
    public int getRenderType()
    {
    	return BuildCraftCore.blockByEntityModel;
    }

	@Override
	public TileEntity getBlockEntity() {
		return new TileRefinery();
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
    public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {	
		if (entityplayer.getCurrentEquippedItem() != null)
			if (entityplayer.getCurrentEquippedItem().getItem() == BuildCraftCore.wrenchItem) {
			
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
			} else {
			
				int liquidId = BuildCraftAPI.getLiquidForFilledItem(entityplayer
						.getCurrentEquippedItem());

				if (liquidId != 0) {
					int qty = ((TileRefinery) world.getBlockTileEntity(i, j, k))
							.fill(Orientations.Unknown,
									BuildCraftAPI.BUCKET_VOLUME, liquidId, true);

					if (qty != 0 && !BuildCraftCore.debugMode) {
						entityplayer.inventory.setInventorySlotContents(
								entityplayer.inventory.currentItem,
								Utils.consumeItem(entityplayer.inventory.getCurrentItem()));
					}

					return true;
				}				
		}
		
		
		FactoryProxy.displayGUIRefinery(world, entityplayer, i, j, k);
			
		return true;		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}
	
}
