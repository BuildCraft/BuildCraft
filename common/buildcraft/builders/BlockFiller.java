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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import buildcraft.BuildCraftBuilders;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.core.DefaultProps;
import buildcraft.core.GuiIds;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;

public class BlockFiller extends BlockContainer {

	int textureSides;
	int textureTopOn;
	int textureTopOff;
	public IFillerPattern currentPattern;

	public BlockFiller(int i) {
		super(i, Material.iron);

		setHardness(0.5F);
		setCreativeTab(CreativeTabs.tabRedstone);

		textureSides = 4 * 16 + 2;
		textureTopOn = 4 * 16 + 0;
		textureTopOff = 4 * 16 + 1;
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {

		// Drop through if the player is sneaking
		if (entityplayer.isSneaking())
			return false;

		if (!CoreProxy.proxy.isRenderWorld(world)) {
			entityplayer.openGui(BuildCraftBuilders.instance, GuiIds.FILLER, world, i, j, k);
		}
		return true;

	}

	@SuppressWarnings({ "all" })
	public int getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k, int l) {
		int m = iblockaccess.getBlockMetadata(i, j, k);

		if (iblockaccess == null)
			return getBlockTextureFromSideAndMetadata(i, m);

		TileEntity tile = iblockaccess.getBlockTileEntity(i, j, k);

		if (tile != null && tile instanceof TileFiller) {
			TileFiller filler = (TileFiller) tile;
			if (l == 1 || l == 0) {
				if (!filler.isActive())
					return textureTopOff;
				else
					return textureTopOn;
			} else if (filler.currentPattern != null)
				return filler.currentPattern.getTextureIndex();
			else
				return textureSides;
		}

		return getBlockTextureFromSideAndMetadata(l, m);
	}

	@Override
	public int getBlockTextureFromSide(int i) {
		if (i == 0 || i == 1)
			return textureTopOn;
		else
			return textureSides;
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TileFiller();
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
		Utils.preDestroyBlock(world, x, y, z);
		super.breakBlock(world, x, y, z, par5, par6);
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}
}
