/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.builders;

import java.util.ArrayList;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftBuilders;
import buildcraft.api.core.Position;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.DefaultProps;
import buildcraft.core.GuiIds;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;

public class BlockBuilder extends BlockContainer {

	int blockTextureTop;
	int blockTextureSide;
	int blockTextureFront;

	public BlockBuilder(int i) {
		super(i, Material.iron);
		blockTextureSide = 3 * 16 + 5;
		blockTextureTop = 3 * 16 + 6;
		blockTextureFront = 3 * 16 + 7;
		setHardness(0.7F);
		setCreativeTab(CreativeTabs.tabRedstone);
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TileBuilder();
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

	@Override
	public int getBlockTextureFromSideAndMetadata(int i, int j) {
		if (j == 0 && i == 3)
			return blockTextureFront;

		if (i == j)
			return blockTextureFront;

		switch (i) {
		case 1:
			return blockTextureTop;
		default:
			return blockTextureSide;
		}
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {

		// Drop through if the player is sneaking
		if (entityplayer.isSneaking())
			return false;

		Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, i, j, k)) {

			int meta = world.getBlockMetadata(i, j, k);

			switch (ForgeDirection.values()[meta]) {
			case WEST:
				world.setBlockMetadata(i, j, k, ForgeDirection.SOUTH.ordinal());
				break;
			case EAST:
				world.setBlockMetadata(i, j, k, ForgeDirection.NORTH.ordinal());
				break;
			case NORTH:
				world.setBlockMetadata(i, j, k, ForgeDirection.WEST.ordinal());
				break;
			case SOUTH:
			default:
				world.setBlockMetadata(i, j, k, ForgeDirection.EAST.ordinal());
				break;
			}

			world.markBlockForUpdate(i, j, k);
			((IToolWrench) equipped).wrenchUsed(entityplayer, i, j, k);
			return true;
		} else {

			if (!CoreProxy.proxy.isRenderWorld(world)) {
				entityplayer.openGui(BuildCraftBuilders.instance, GuiIds.BUILDER, world, i, j, k);
			}
			return true;

		}
	}

	@Override
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLiving entityliving) {
		super.onBlockPlacedBy(world, i, j, k, entityliving);
		ForgeDirection orientation = Utils.get2dOrientation(new Position(entityliving.posX, entityliving.posY, entityliving.posZ), new Position(i, j, k));

		world.setBlockMetadataWithNotify(i, j, k, orientation.getOpposite().ordinal());
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
		Utils.preDestroyBlock(world, x, y, z);
		super.breakBlock(world, x, y, z, par5, par6);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}

}
