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
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.BuildCraftAPI;
import net.minecraft.src.buildcraft.api.liquids.LiquidManager;
import net.minecraft.src.buildcraft.api.liquids.LiquidStack;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.forge.ITextureProvider;

public class BlockTank extends BlockContainer implements ITextureProvider {

	public BlockTank(int i) {
		super(i, Material.glass);

		setBlockBounds(0.125F, 0F, 0.125F, 0.875F, 1F, 0.875F);
		setHardness(0.5F);

	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	public boolean isACube() {
		return false;
	}

	@Override
	public TileEntity getBlockEntity() {
		return new TileTank();
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

	@Override
	public int getBlockTextureFromSide(int i) {
		switch (i) {
		case 0:
		case 1:
			return 6 * 16 + 2;
		default:
			return 6 * 16 + 0;
		}
	}

	@SuppressWarnings({ "all" })
	public int getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k, int l) {
		switch (l) {
		case 0:
		case 1:
			return 6 * 16 + 2;
		default:
			if (iblockaccess.getBlockId(i, j - 1, k) == blockID) {
				return 6 * 16 + 1;
			} else {
				return 6 * 16 + 0;
			}
		}
	}

	@Override
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {

		ItemStack current = entityplayer.inventory.getCurrentItem();
		if (current != null) {
			
			LiquidStack liquid = LiquidManager.getLiquidForFilledItem(current);

			TileTank tank = (TileTank) world.getBlockTileEntity(i, j, k);

			// Handle filled containers
			if (liquid != null) {
				int qty = tank.fill(Orientations.Unknown, liquid, true);

				if (qty != 0 && !BuildCraftCore.debugMode) {
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem,
							Utils.consumeItem(current));
				}

				return true;
			
			// Handle empty containers
			} else {
				
				LiquidStack available = tank.getTanks()[0].getLiquid();
				ItemStack filled = LiquidManager.fillLiquidContainer(available, current);
					
				liquid = LiquidManager.getLiquidForFilledItem(filled);
				if(liquid != null) {
					
					if(current.stackSize > 1) {
						if(!entityplayer.inventory.addItemStackToInventory(filled))
							return false;
						else
							entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem,
									Utils.consumeItem(current));
					} else {
						entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem,
								Utils.consumeItem(current));
						entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, filled);
					}
					
					tank.drain(Orientations.Unknown, liquid.amount, true);
					return true;
				}
			}
		}

		return false;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}

}
