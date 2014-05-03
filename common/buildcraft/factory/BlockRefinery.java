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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftFactory;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.GuiIds;
import buildcraft.core.fluids.FluidUtils;
import buildcraft.core.utils.Utils;

public class BlockRefinery extends BlockContainer {

	private static IIcon icon;

	public BlockRefinery() {
		super(Material.iron);

		setHardness(5F);
		setCreativeTab(CreativeTabBuildCraft.BLOCKS.get());
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
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
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLivingBase entityliving, ItemStack stack) {
		super.onBlockPlacedBy(world, i, j, k, entityliving, stack);

		ForgeDirection orientation = Utils.get2dOrientation(entityliving);

		world.setBlockMetadataWithNotify(i, j, k, orientation.getOpposite().ordinal(), 1);
	}

	@Override
	public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis) {
		int meta = world.getBlockMetadata(x, y, z);

		switch (ForgeDirection.getOrientation(meta)) {
			case WEST:
				world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.SOUTH.ordinal(), 3);
				break;
			case EAST:
				world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.NORTH.ordinal(), 3);
				break;
			case NORTH:
				world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.WEST.ordinal(), 3);
				break;
			case SOUTH:
			default:
				world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.EAST.ordinal(), 3);
				break;
		}
		world.markBlockForUpdate(x, y, z);
		return true;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		TileEntity tile = world.getTileEntity(x, y, z);

		if (!(tile instanceof TileRefinery)) {
			return false;
		}

		ItemStack current = player.getCurrentEquippedItem();
		Item equipped = current != null ? current.getItem() : null;
		if (player.isSneaking() && equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(player, x, y, z)) {
			((TileRefinery) tile).resetFilters();
			((IToolWrench) equipped).wrenchUsed(player, x, y, z);
			return true;
		}

		if (current != null && current.getItem() != Items.bucket) {
			if (!world.isRemote) {
				if (FluidUtils.handleRightClick((TileRefinery) tile, ForgeDirection.getOrientation(side), player, true, false)) {
					return true;
				}
			} else if (FluidContainerRegistry.isContainer(current)) {
				return true;
			}
		}

		if (!world.isRemote) {
			player.openGui(BuildCraftFactory.instance, GuiIds.REFINERY, world, x, y, z);
		}

		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		icon = par1IconRegister.registerIcon("buildcraft:refineryBack");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int par1, int par2) {
		return icon;
	}
}
