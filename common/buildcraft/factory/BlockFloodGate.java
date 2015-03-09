/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.api.tools.IToolWrench;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.core.lib.utils.Utils;

public class BlockFloodGate extends BlockBuildCraft {
	public BlockFloodGate() {
		super(Material.iron);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileFloodGate();
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		if (super.onBlockActivated(world, i, j, k, entityplayer, par6, par7, par8, par9)) {
			return true;
		}

		TileEntity tile = world.getTileEntity(i, j, k);

		if (tile instanceof TileFloodGate) {
			TileFloodGate floodGate = (TileFloodGate) tile;

			// Drop through if the player is sneaking
			if (entityplayer.isSneaking()) {
				return false;
			}

			// Restart the quarry if its a wrench
			Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
			if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, i, j, k)) {

				floodGate.rebuildQueue();
				((IToolWrench) equipped).wrenchUsed(entityplayer, i, j, k);
				return true;
			}
		}

		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		super.onNeighborBlockChange(world, x, y, z, block);
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof TileFloodGate) {
			((TileFloodGate) tile).onNeighborBlockChange(block);
		}
	}
}
