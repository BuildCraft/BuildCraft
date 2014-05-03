/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
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
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.utils.Utils;

public class BlockPump extends BlockBuildCraft {

	private IIcon textureTop;
	private IIcon textureBottom;
	private IIcon textureSide;

	public BlockPump() {
		super(Material.iron);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TilePump();
	}

	@Override
	public IIcon getIcon(int i, int j) {
		switch (i) {
			case 0:
				return textureBottom;
			case 1:
				return textureTop;
			default:
				return textureSide;
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int par6) {
		Utils.preDestroyBlock(world, x, y, z);
		super.breakBlock(world, x, y, z, block, par6);
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		TileEntity tile = world.getTileEntity(i, j, k);

		if (tile instanceof TilePump) {
			TilePump pump = (TilePump) tile;

			// Drop through if the player is sneaking
			if (entityplayer.isSneaking()) {
				return false;
			}

			// Restart the quarry if its a wrench
			Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
			if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, i, j, k)) {

				pump.tank.reset();
				pump.rebuildQueue();
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
		if (tile instanceof TilePump) {
			((TilePump) tile).onNeighborBlockChange(block);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		textureTop = par1IconRegister.registerIcon("buildcraft:pump_top");
		textureBottom = par1IconRegister.registerIcon("buildcraft:pump_bottom");
		textureSide = par1IconRegister.registerIcon("buildcraft:pump_side");
	}
}
