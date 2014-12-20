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
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftFactory;
import buildcraft.api.events.BlockInteractionEvent;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.GuiIds;
import buildcraft.core.fluids.TankUtils;
import buildcraft.core.utils.Utils;

public class BlockRefinery extends BlockBuildCraft {

	//private static IIcon icon;

	public BlockRefinery() {
		super(Material.iron);

		setHardness(5F);
		setCreativeTab(CreativeTabBuildCraft.BLOCKS.get());
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}


	public boolean isACube() {
		return false;
	}

	@Override
	public int getRenderType() {
		return BuildCraftCore.blockByEntityModel;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileRefinery();
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityliving, ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, entityliving, stack);

		EnumFacing orientation = Utils.get2dOrientation(entityliving);

		world.setBlockState(pos, state.withProperty(FACING_PROP, orientation.getOpposite()), 1);
	}

	@Override
	public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
		IBlockState state = world.getBlockState(pos);
		EnumFacing side = (EnumFacing)state.getValue(FACING_PROP);

		switch (side) {
			case WEST:
				world.setBlockState(pos, state.withProperty(FACING_PROP, EnumFacing.SOUTH), 3);
				break;
			case EAST:
				world.setBlockState(pos, state.withProperty(FACING_PROP, EnumFacing.NORTH), 3);
				break;
			case NORTH:
				world.setBlockState(pos, state.withProperty(FACING_PROP, EnumFacing.WEST), 3);
				break;
			case SOUTH:
			default:
				world.setBlockState(pos, state.withProperty(FACING_PROP, EnumFacing.EAST), 3);
				break;
		}
		world.markBlockForUpdate(pos);
		return true;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing face, float hitX, float hitY, float hitZ) {
		TileEntity tile = world.getTileEntity(pos);

		if (!(tile instanceof TileRefinery)) {
			return false;
		}
		BlockInteractionEvent event = new BlockInteractionEvent(player, pos, state);
		FMLCommonHandler.instance().bus().post(event);
		if (event.isCanceled()) {
			 return false;
		}

		ItemStack current = player.getCurrentEquippedItem();
		Item equipped = current != null ? current.getItem() : null;
		if (player.isSneaking() && equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(player, pos)) {
			((TileRefinery) tile).resetFilters();
			((IToolWrench) equipped).wrenchUsed(player, pos);
			return true;
		}

		if (current != null && current.getItem() != Items.bucket) {
			if (!world.isRemote) {
				if (TankUtils.handleRightClick((TileRefinery) tile, face, player, true, false)) {
					return true;
				}
			} else if (FluidContainerRegistry.isContainer(current)) {
				return true;
			}
		}


		if (!world.isRemote) {
			player.openGui(BuildCraftFactory.instance, GuiIds.REFINERY, world, pos.getX(), pos.getY(), pos.getZ());
		}

		return true;
	}

	/*@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		icon = par1IconRegister.registerIcon("buildcraft:refineryBack");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int par1, int par2) {
		return icon;
	}*/
}
