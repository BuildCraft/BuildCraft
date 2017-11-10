/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftFactory;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.GuiIds;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.core.lib.fluids.TankUtils;

public class BlockRefinery extends BlockBuildCraft {
	public BlockRefinery() {
		super(Material.iron);

		setHardness(5F);
		setCreativeTab(BCCreativeTab.get("main"));
		setRotatable(true);
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
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
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if (super.onBlockActivated(world, x, y, z, player, side, hitX, hitY, hitZ)) {
			return true;
		}

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
				if (TankUtils.handleRightClick((TileRefinery) tile, ForgeDirection.getOrientation(side), player, true, false)) {
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
}
