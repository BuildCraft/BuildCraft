/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.api.events.BlockInteractionEvent;
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
		TileConstructionMarker marker = (TileConstructionMarker) world.getTileEntity(x, y, z);
		if (marker != null && marker.itemBlueprint != null && !world.isRemote) {
			float f1 = 0.7F;
			double d = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
			double d1 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
			double d2 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
			EntityItem itemToDrop = new EntityItem(world, x + d, y + d1, z + d2, marker.itemBlueprint);
			itemToDrop.delayBeforeCanPickup = 10;
			world.spawnEntityInWorld(itemToDrop);
		}
		super.breakBlock(world, x, y, z, block, par6);
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
			}
		}

		return true;
	}
}
