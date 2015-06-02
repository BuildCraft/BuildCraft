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
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import buildcraft.BuildCraftCore;
import buildcraft.api.events.BlockInteractionEvent;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.utils.Utils;

public class BlockTank extends BlockBuildCraft {

	public BlockTank() {
		super(Material.glass,new IProperty[]{JOINED_BELOW});
		// Default to JOINED_BELOW being false
		setDefaultState(getDefaultState().cycleProperty(JOINED_BELOW));
		
		setBlockBounds(0.125F, 0F, 0.125F, 0.875F, 1F, 0.875F);
		setHardness(0.5F);
		setCreativeTab(CreativeTabBuildCraft.BLOCKS.get());
	}
	
	@Override
	public boolean isFullCube() { return false; }

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer() {
		return EnumWorldBlockLayer.CUTOUT;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileTank();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumFacing side, float hitX, float hitY, float hitZ) {
		ItemStack current = entityplayer.inventory.getCurrentItem();

		BlockInteractionEvent event = new BlockInteractionEvent(entityplayer, pos, state);
		FMLCommonHandler.instance().bus().post(event);
		if (event.isCanceled()) {
			return false;
		}

		//Investigate if EnumFacing.NORTH don't break anything
		if (current != null) {
			TileEntity tile = world.getTileEntity(pos);

			if (tile instanceof TileTank) {
				TileTank tank = (TileTank) tile;
				// Handle FluidContainerRegistry
				if (FluidContainerRegistry.isContainer(current)) {
					FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(current);
					// Handle filled containers
					if (liquid != null) {
						int qty = tank.fill(EnumFacing.NORTH, liquid, true);

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
						FluidStack available = tank.getTankInfo(EnumFacing.NORTH)[0].fluid;

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

								tank.drain(EnumFacing.NORTH, liquid.amount, true);

								return true;
							}
						}
					}
				} else if (current.getItem() instanceof IFluidContainerItem) {
					if (!world.isRemote) {
						IFluidContainerItem container = (IFluidContainerItem) current.getItem();
						FluidStack liquid = container.getFluid(current);
						FluidStack tankLiquid = tank.getTankInfo(EnumFacing.NORTH)[0].fluid;
						boolean mustDrain = (liquid == null || liquid.amount == 0);
						boolean mustFill = (tankLiquid == null || tankLiquid.amount == 0);
						if (mustDrain && mustFill) {
							// Both are empty, do nothing
						} else if (mustDrain || !entityplayer.isSneaking()) {
							liquid = tank.drain(EnumFacing.NORTH, 1000, false);
							int qtyToFill = container.fill(current, liquid, true);
							tank.drain(EnumFacing.NORTH, qtyToFill, true);
						} else if (mustFill || entityplayer.isSneaking()) {
							if (liquid != null && liquid.amount > 0) {
								int qty = tank.fill(EnumFacing.NORTH, liquid, false);
								tank.fill(EnumFacing.NORTH, container.drain(current, qty, true), true);
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
	public boolean shouldSideBeRendered(IBlockAccess world, BlockPos pos, EnumFacing side) {
		if (side.getIndex() <= 1) {
			return world.getBlockState(pos).getBlock() != this;
		} else {
			return super.shouldSideBeRendered(world, pos, side);
		}
	}

	@Override
	public int getLightValue(IBlockAccess world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);

		if (tile instanceof TileTank) {
			TileTank tank = (TileTank) tile;
			return tank.getFluidLightLevel();
		}

		return super.getLightValue(world, pos);
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityliving, ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, entityliving, stack);

		IBlockState below = world.getBlockState(pos.down());
		world.setBlockState(pos, state.withProperty(JOINED_BELOW, below.getBlock() == this));

		IBlockState above = world.getBlockState(pos.up());
		if (above.getBlock() == this) {
			world.setBlockState(pos.up(), above.withProperty(JOINED_BELOW, true));
		}
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		Utils.preDestroyBlock(world, pos, state);
		super.breakBlock(world, pos, state);

		IBlockState above = world.getBlockState(pos.up());
		if (above.getBlock() == this) {
			world.setBlockState(pos.up(), above.withProperty(JOINED_BELOW, false));
		}
	}
}
