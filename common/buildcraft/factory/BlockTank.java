/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.factory;

import java.util.ArrayList;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import buildcraft.BuildCraftCore;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.utils.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockTank extends BlockContainer {

	private Icon textureStackedSide;
    private Icon textureBottomSide;
    private Icon textureTop;

    public BlockTank(int i) {
		super(i, Material.glass);
		setBlockBounds(0.125F, 0F, 0.125F, 0.875F, 1F, 0.875F);
		setHardness(0.5F);
		setCreativeTab(CreativeTabBuildCraft.tabBuildCraft);
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
	public TileEntity createNewTileEntity(World var1) {
		return new TileTank();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int par1, int par2) {
		switch(par1){
		case 0:
		case 1:
			return textureTop;
		default:
			return textureBottomSide;
		}
	}

	@SuppressWarnings({ "all" })
	public Icon getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k, int l) {
		switch (l) {
		case 0:
		case 1:
			return textureTop;
		default:
			if (iblockaccess.getBlockId(i, j - 1, k) == blockID)
				return textureStackedSide;
			else
				return textureBottomSide;
		}
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {

		ItemStack current = entityplayer.inventory.getCurrentItem();
		if (current != null) {

			LiquidStack liquid = LiquidContainerRegistry.getLiquidForFilledItem(current);

			TileTank tank = (TileTank) world.getBlockTileEntity(i, j, k);

			// Handle filled containers
			if (liquid != null) {
				int qty = tank.fill(ForgeDirection.UNKNOWN, liquid, true);

				if (qty != 0 && !BuildCraftCore.debugMode && !entityplayer.capabilities.isCreativeMode) {
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, Utils.consumeItem(current));
				}

				return true;

				// Handle empty containers
			} else {

				LiquidStack available = tank.getTanks(ForgeDirection.UNKNOWN)[0].getLiquid();
				if (available != null) {
					ItemStack filled = LiquidContainerRegistry.fillLiquidContainer(available, current);

					liquid = LiquidContainerRegistry.getLiquidForFilledItem(filled);

					if (liquid != null) {
						if (!BuildCraftCore.debugMode && !entityplayer.capabilities.isCreativeMode) {
							if (current.stackSize > 1) {
								if (!entityplayer.inventory.addItemStackToInventory(filled))
									return false;
								else {
									entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, Utils.consumeItem(current));
								}
							} else {
								entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, Utils.consumeItem(current));
								entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, filled);
							}
						}
						tank.drain(ForgeDirection.UNKNOWN, liquid.amount, true);
						return true;
					}
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

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
	    textureStackedSide = par1IconRegister.registerIcon("buildcraft:tank_stacked_side");
        textureBottomSide = par1IconRegister.registerIcon("buildcraft:tank_bottom_side");
        textureTop = par1IconRegister.registerIcon("buildcraft:tank_top");
	}

}
