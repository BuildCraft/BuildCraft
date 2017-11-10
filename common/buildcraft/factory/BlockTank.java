/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import buildcraft.BuildCraftCore;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.core.lib.inventory.InvUtils;

public class BlockTank extends BlockBuildCraft {
	private static final boolean DEBUG_MODE = false;
	private IIcon textureStackedSide;

	public BlockTank() {
		super(Material.glass);
		setBlockBounds(0.125F, 0F, 0.125F, 0.875F, 1F, 0.875F);
		setHardness(0.5F);
		setCreativeTab(BCCreativeTab.get("main"));
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int par6) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile != null && tile instanceof TileTank) {
			TileTank tank = (TileTank) tile;
			tank.onBlockBreak();
		}

		TileEntity tileAbove = world.getTileEntity(x, y + 1, z);
		TileEntity tileBelow = world.getTileEntity(x, y - 1, z);

		super.breakBlock(world, x, y, z, block, par6);

		if (tileAbove instanceof TileTank) {
			((TileTank) tileAbove).updateComparators();
		}

		if (tileBelow instanceof TileTank) {
			((TileTank) tileBelow).updateComparators();
		}
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

	@SuppressWarnings({"all"})
	@Override
	public IIcon getIconAbsolute(IBlockAccess iblockaccess, int i, int j, int k, int side, int metadata) {
		if (side >= 2 && iblockaccess.getBlock(i, j - 1, k) instanceof BlockTank) {
			return textureStackedSide;
		} else {
			return super.getIconAbsolute(side, metadata);
		}
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		if (super.onBlockActivated(world, i, j, k, entityplayer, par6, par7, par8, par9)) {
			return true;
		}

		ItemStack current = entityplayer.inventory.getCurrentItem();

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
									entityplayer.dropPlayerItemWithRandomChoice(FluidContainerRegistry.drainFluidContainer(current), false);
								}

								entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, InvUtils.consumeItem(current));
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
					if (current.stackSize != 1) {
						return false;
					}

					if (!world.isRemote) {
						IFluidContainerItem container = (IFluidContainerItem) current.getItem();
						FluidStack liquid = container.getFluid(current);
						FluidStack tankLiquid = tank.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid;
						boolean mustDrain = liquid == null || liquid.amount == 0;
						boolean mustFill = tankLiquid == null || tankLiquid.amount == 0;
						if (mustDrain && mustFill) {
							// Both are empty, do nothing
						} else if (mustDrain || !entityplayer.isSneaking()) {
							liquid = tank.drain(ForgeDirection.UNKNOWN, 1000, false);
							int qtyToFill = container.fill(current, liquid, true);
							tank.drain(ForgeDirection.UNKNOWN, qtyToFill, true);
						} else if (mustFill || entityplayer.isSneaking()) {
							if (liquid.amount > 0) {
								int qty = tank.fill(ForgeDirection.UNKNOWN, liquid, false);
								tank.fill(ForgeDirection.UNKNOWN, container.drain(current, qty, true), true);
							}
						}
					}

					return true;
				}
			}
		} else if (DEBUG_MODE) {
			TileEntity tile = world.getTileEntity(i, j, k);

			if (tile instanceof TileTank) {
				TileTank tank = (TileTank) tile;
				if (tank.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid != null) {
					entityplayer.addChatComponentMessage(new ChatComponentText("Amount: " + tank.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid.amount + " mB"));
				}
			}
		}

		return false;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
		if (side <= 1) {
			return !(world.getBlock(x, y, z) instanceof BlockTank);
		} else {
			return super.shouldSideBeRendered(world, x, y, z, side);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		super.registerBlockIcons(par1IconRegister);
		textureStackedSide = par1IconRegister.registerIcon("buildcraftfactory:tankBlock/side_stacked");
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

	@Override
	public boolean hasComparatorInputOverride() {
		return true;
	}

	@Override
	public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
		TileEntity tile = world.getTileEntity(x, y, z);

		if (tile instanceof TileTank) {
			TileTank tank = (TileTank) tile;
			return tank.getComparatorInputOverride();
		}

		return 0;
	}
}
