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

import net.minecraft.src.BlockContainer;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import buildcraft.BuildCraftBuilders;
import buildcraft.core.DefaultProps;
import buildcraft.core.GuiIds;
import buildcraft.core.ProxyCore;


public class BlockBlueprintLibrary extends BlockContainer {

	public BlockBlueprintLibrary(int i) {
		super(i, Material.wood);
		setCreativeTab(CreativeTabs.tabRedstone);
		setHardness(0.7F);
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		super.onBlockActivated(world, i, j, k, entityplayer, par6, par7, par8, par9);

		// Drop through if the player is sneaking
		if (entityplayer.isSneaking())
			return false;

		TileBlueprintLibrary tile = (TileBlueprintLibrary) world.getBlockTileEntity(i, j, k);

		if (!tile.locked || entityplayer.username.equals(tile.owner))
			if (!ProxyCore.proxy.isRemote(world))
				entityplayer.openGui(BuildCraftBuilders.instance, GuiIds.BLUEPRINT_LIBRARY, world, i, j, k);

		return true;
	}


	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TileBlueprintLibrary();
	}

	@Override
	public int getBlockTextureFromSide(int i) {
		switch (i) {
		case 0:
		case 1:
			return 3 * 16 + 5;
		default:
			return 3 * 16 + 8;
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLiving entityliving) {
		if (entityliving instanceof EntityPlayer) {
			TileBlueprintLibrary tile = (TileBlueprintLibrary) world.getBlockTileEntity(i, j, k);

			tile.owner = ((EntityPlayer) entityliving).username;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}
}
