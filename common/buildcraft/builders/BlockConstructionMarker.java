/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import buildcraft.api.tools.IToolWrench;
import buildcraft.core.BlockMarker;
import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.core.lib.utils.Utils;

public class BlockConstructionMarker extends BlockMarker {
	public BlockConstructionMarker() {
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileConstructionMarker();
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int par6) {
		Utils.preDestroyBlock(world, x, y, z);
		dropMarkerIfPresent(world, x, y, z, true);
		super.breakBlock(world, x, y, z, block, par6);
	}

	private boolean dropMarkerIfPresent(World world, int x, int y, int z, boolean onBreak) {
		TileConstructionMarker marker = (TileConstructionMarker) world.getTileEntity(x, y, z);
		if (marker != null && marker.itemBlueprint != null && !world.isRemote) {
			BlockUtils.dropItem((WorldServer) world, x, y, z, 6000, marker.itemBlueprint);
			marker.itemBlueprint = null;
			if (!onBreak) {
				marker.bluePrintBuilder = null;
				marker.bptContext = null;
				marker.sendNetworkUpdate();
			}
			return true;
		}
		return false;
	}

	@Override
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLivingBase entityliving, ItemStack stack) {
		super.onBlockPlacedBy(world, i, j, k, entityliving, stack);

		TileConstructionMarker tile = (TileConstructionMarker) world.getTileEntity(i, j, k);
		tile.direction = Utils.get2dOrientation(entityliving);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int par6, float par7,
									float par8, float par9) {
		if (super.onBlockActivated(world, x, y, z, entityplayer, par6, par7, par8, par9)) {
			return true;
		}

		TileConstructionMarker marker = (TileConstructionMarker) world.getTileEntity(x, y, z);

		Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem()
				: null;

		if (equipped instanceof ItemBlueprint) {
			if (marker.itemBlueprint == null) {
				ItemStack stack = entityplayer.inventory.getCurrentItem().copy();
				stack.stackSize = 1;
				marker.setBlueprint(stack);
				stack = null;
				if (entityplayer.inventory.getCurrentItem().stackSize > 1) {
					stack = entityplayer.getCurrentEquippedItem().copy();
					stack.stackSize = entityplayer.getCurrentEquippedItem().stackSize - 1;
				}
				entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, stack);

				return true;
			}
		} else if (equipped instanceof ItemConstructionMarker) {
			if (ItemConstructionMarker.linkStarted(entityplayer.getCurrentEquippedItem())) {
				ItemConstructionMarker.link(entityplayer.getCurrentEquippedItem(), world, x, y, z);
				return true;
			}
		} else if ((equipped == null || equipped instanceof IToolWrench) && entityplayer.isSneaking()) {
			return dropMarkerIfPresent(world, x, y, z, false);
		}

		return false;
	}
}
