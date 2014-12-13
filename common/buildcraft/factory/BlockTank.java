/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import buildcraft.BuildCraftCore;
import buildcraft.api.events.BlockInteractionEvent;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.inventory.InvUtils;

public class BlockTank extends BlockBuildCraft {

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

		BlockInteractionEvent event = new BlockInteractionEvent(entityplayer, this);
		FMLCommonHandler.instance().bus().post(event);
		if (event.isCanceled()) {
			return false;
		}

		if (current != null) {
			TileEntity tile = world.getTileEntity(i, j, k);

			if (tile instanceof TileTank) {
				TileTank tank = (TileTank) tile;
				// Handle FluidContainerRegistry
				if (FluidContainerRegistry.isContainer(current)) {
					FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(current);
					// Handle filled containers
					if (liquid != null) {
						int qty = tank.fill(ForgeDirection.UNKNOWN, liquid, true);

						if (qty != 0 && !BuildCraftCore.debugWorldgen && !entityplayer.capabilities.isCreativeMode) {
							if (current.stackSize > 1) {
								if (!entityplayer.inventory.addItemStackToInventory(FluidContainerRegistry.drainFluidContainer(current))) {
									return false;
								} else {
									entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, InvUtils.consumeItem(current));
								}
							} else {
								entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, FluidContainerRegistry.drainFluidContainer(current));
							}
						}

						return true;
						// Handle empty containers
					} else {
						FluidStack available = tank.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid;

						if (available != null) {
							ItemStack filled = FluidContainerRegistry.fillFluidContainer(available, current);

							liquid = FluidContainerRegistry.getFluidForFilledItem(filled);

							if (liquid != null) {
								if (!BuildCraftCore.debugWorldgen && !entityplayer.capabilities.isCreativeMode) {
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
				} else if (current.getItem() instanceof IFluidContainerItem) {
					if (!world.isRemote) {
						IFluidContainerItem container = (IFluidContainerItem) current.getItem();
						FluidStack liquid = container.getFluid(current);
						FluidStack tankLiquid = tank.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid;
						boolean mustDrain = (liquid == null || liquid.amount == 0);
						boolean mustFill = (tankLiquid == null || tankLiquid.amount == 0);
						if (mustDrain && mustFill) {
							// Both are empty, do nothing
						} else if (mustDrain || !entityplayer.isSneaking()) {
							liquid = tank.drain(ForgeDirection.UNKNOWN, 1000, false);
							int qtyToFill = container.fill(current, liquid, true);
							tank.drain(ForgeDirection.UNKNOWN, qtyToFill, true);
						} else if (mustFill || entityplayer.isSneaking()) {
							if (liquid != null && liquid.amount > 0) {
								int qty = tank.fill(ForgeDirection.UNKNOWN, liquid, false);
								tank.fill(ForgeDirection.UNKNOWN, container.drain(current, qty, true), true);
							}
						}
					}

					return true;
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
