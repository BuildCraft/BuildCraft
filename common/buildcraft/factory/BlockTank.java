/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.BuildCraftCore;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.inventory.InvUtils;

public class BlockTank extends BlockContainer {

	private IIcon textureStackedSide;
	private IIcon textureBottomSide;
	private IIcon textureTop;

	public BlockTank() {
		super(Material.glass);
		setBlockBounds(0.125F, 0F, 0.125F, 0.875F, 1F, 0.875F);
		setHardness(0.5F);
		setCreativeTab(CreativeTabBuildCraft.BLOCKS.get());
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileTank();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int par1, int par2) {
		switch (par1) {
			case 0:
			case 1:
				return textureTop;
			default:
				return textureBottomSide;
		}
	}

	@SuppressWarnings({"all"})
	@Override
	public IIcon getIcon(IBlockAccess iblockaccess, int i, int j, int k, int l) {
		switch (l) {
			case 0:
			case 1:
				return textureTop;
			default:
				if (iblockaccess.getBlock(i, j - 1, k) == this) {
					return textureStackedSide;
				} else {
					return textureBottomSide;
				}
		}
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		ItemStack current = entityplayer.inventory.getCurrentItem();

		if (current != null) {
			FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(current);

			TileEntity tile = world.getTileEntity(i, j, k);

			if (tile instanceof TileTank) {
				TileTank tank = (TileTank) tile;
				// Handle filled containers
				if (liquid != null) {
					int qty = tank.fill(ForgeDirection.UNKNOWN, liquid, true);

					if (qty != 0 && !BuildCraftCore.debugMode && !entityplayer.capabilities.isCreativeMode) {
						entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, InvUtils.consumeItem(current));
					}

					return true;

					// Handle empty containers
				} else {
					FluidStack available = tank.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid;

					if (available != null) {
						ItemStack filled = FluidContainerRegistry.fillFluidContainer(available, current);

						liquid = FluidContainerRegistry.getFluidForFilledItem(filled);

						if (liquid != null) {
							if (!BuildCraftCore.debugMode && !entityplayer.capabilities.isCreativeMode) {
								if (current.stackSize > 1) {
									if (!entityplayer.inventory.addItemStackToInventory(filled)) {
										return false;
									} else {
										entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, InvUtils.consumeItem(current));
									}
								} else {
									entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, InvUtils.consumeItem(current));
									entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, filled);
								}
							}

							tank.drain(ForgeDirection.UNKNOWN, liquid.amount, true);

							return true;
						}
					}
				}
			}
		}

		return false;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
		if (side <= 1) {
			return world.getBlock(x, y, z) != this;
		} else {
			return super.shouldSideBeRendered(world, x, y, z, side);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		textureStackedSide = par1IconRegister.registerIcon("buildcraft:tank_stacked_side");
		textureBottomSide = par1IconRegister.registerIcon("buildcraft:tank_bottom_side");
		textureTop = par1IconRegister.registerIcon("buildcraft:tank_top");
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z) {
		TileEntity tile = world.getTileEntity(x, y, z);

		if (tile instanceof TileTank) {
			TileTank tank = (TileTank) tile;
			return tank.getFluidLightLevel();
		}

		return super.getLightValue(world, x, y, z);
	}
}
