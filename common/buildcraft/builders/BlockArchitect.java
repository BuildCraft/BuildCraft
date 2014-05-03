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
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftBuilders;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.BlockMultiTexture;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.GuiIds;
import buildcraft.core.utils.Utils;

public class BlockArchitect extends BlockMultiTexture {

	public BlockArchitect() {
		super(Material.iron, CreativeTabBuildCraft.BLOCKS);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileArchitect();
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {

		// Drop through if the player is sneaking
		if (entityplayer.isSneaking()) {
			return false;
		}

		Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, i, j, k)) {

			int meta = world.getBlockMetadata(i, j, k);

			switch (ForgeDirection.values()[meta]) {
			case WEST:
				world.setBlockMetadataWithNotify(i, j, k, ForgeDirection.SOUTH.ordinal(), 0);
				break;
			case EAST:
				world.setBlockMetadataWithNotify(i, j, k, ForgeDirection.NORTH.ordinal(), 0);
				break;
			case NORTH:
				world.setBlockMetadataWithNotify(i, j, k, ForgeDirection.WEST.ordinal(), 0);
				break;
			case SOUTH:
			default:
				world.setBlockMetadataWithNotify(i, j, k, ForgeDirection.EAST.ordinal(), 0);
				break;
			}

			world.markBlockForUpdate(i, j, k);
			((IToolWrench) equipped).wrenchUsed(entityplayer, i, j, k);
			return true;
		} else {

			if (!world.isRemote) {
				entityplayer.openGui(BuildCraftBuilders.instance, GuiIds.ARCHITECT_TABLE, world, i, j, k);
			}
			return true;

		}
	}

	@Override
	public void breakBlock(World world, int i, int j, int k, Block block, int par6) {
		Utils.preDestroyBlock(world, i, j, k);

		super.breakBlock(world, i, j, k, block, par6);
	}

	@Override
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLivingBase entityliving, ItemStack stack) {
		super.onBlockPlacedBy(world, i, j, k, entityliving, stack);

		ForgeDirection orientation = Utils.get2dOrientation(entityliving);

		world.setBlockMetadataWithNotify(i, j, k, orientation.getOpposite().ordinal(), 1);
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		return false;
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z) {
		return 1;
	}

	/* MULTI TEXTURE */
	@Override
	public String getIconPrefix() {
		return "architect_";
	}

	@Override
	public int getFrontSide(IBlockAccess world, int x, int y, int z) {
		return world.getBlockMetadata(x, y, z);
	}

}
