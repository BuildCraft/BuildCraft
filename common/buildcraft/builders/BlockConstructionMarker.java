/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.api.events.BlockInteractionEvent;
import buildcraft.core.utils.Utils;

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
		super.breakBlock(world, x, y, z, block, par6);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		blockIcon = par1IconRegister.registerIcon("buildcraft:constructMarker");
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
		super.onBlockActivated(world, x, y, z, entityplayer, par6, par7, par8, par9);

		BlockInteractionEvent event = new BlockInteractionEvent(entityplayer, this);
		FMLCommonHandler.instance().bus().post(event);
        if (event.isCanceled()) {
            return false;
        }

		TileConstructionMarker marker = (TileConstructionMarker) world.getTileEntity(x, y, z);

		Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem()
				: null;

		if (equipped instanceof ItemBlueprint) {
			if (marker.itemBlueprint == null) {
				marker.setBlueprint(entityplayer.inventory.getCurrentItem().copy());
				entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, null);

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
