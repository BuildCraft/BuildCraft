/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import buildcraft.BuildCraftTransport;
import buildcraft.api.transport.IItemPipe;
import buildcraft.core.GuiIds;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.core.lib.block.IComparatorInventory;

public class BlockFilteredBuffer extends BlockBuildCraft implements IComparatorInventory {
	public BlockFilteredBuffer() {
		super(Material.iron);
		setHardness(5F);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileFilteredBuffer();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer,
									int par6, float par7, float par8, float par9) {
		if (super.onBlockActivated(world, x, y, z, entityplayer, par6, par7, par8, par9)) {
			return true;
		}

		if (entityplayer.isSneaking()) {
			return false;
		}

		if (entityplayer.getCurrentEquippedItem() != null) {
			if (entityplayer.getCurrentEquippedItem().getItem() instanceof IItemPipe) {
				return false;
			}
		}

		if (!world.isRemote) {
			entityplayer.openGui(BuildCraftTransport.instance, GuiIds.FILTERED_BUFFER, world, x, y, z);
		}

		return true;
	}

	@Override
	public boolean doesSlotCountComparator(TileEntity tile, int slot, ItemStack stack) {
		return ((TileFilteredBuffer) tile).getFilters().getStackInSlot(slot) != null;
	}
}
