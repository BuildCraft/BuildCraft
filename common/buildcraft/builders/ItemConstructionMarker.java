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
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.api.core.Position;
import buildcraft.core.lib.utils.NBTUtils;

public class ItemConstructionMarker extends ItemBlock {

	@SideOnly(Side.CLIENT)
	public IIcon iconBase;

	@SideOnly(Side.CLIENT)
	public IIcon iconRecording;

	public ItemConstructionMarker(Block block) {
		super(block);
	}

	public static boolean linkStarted(ItemStack marker) {
		return NBTUtils.getItemData(marker).hasKey("x");
	}

	public static void link(ItemStack marker, World world, int x, int y, int z) {
		NBTTagCompound nbt = NBTUtils.getItemData(marker);

		if (nbt.hasKey("x")) {
			int ox = nbt.getInteger("x");
			int oy = nbt.getInteger("y");
			int oz = nbt.getInteger("z");

			TileEntity tile1 = world.getTileEntity(ox, oy, oz);

			if (!new Position(ox, oy, oz).isClose(new Position(x, y, z), 64)) {
				return;
			}

			if (tile1 != null && (tile1 instanceof TileArchitect)) {
				TileArchitect architect = (TileArchitect) tile1;
				TileEntity tile2 = world.getTileEntity(x, y, z);

				if (tile1 != tile2 && tile2 != null) {
					if (tile2 instanceof TileArchitect
							|| tile2 instanceof TileConstructionMarker
							|| tile2 instanceof TileBuilder) {
						architect.addSubBlueprint(tile2);

						nbt.removeTag("x");
						nbt.removeTag("y");
						nbt.removeTag("z");
					}
				}

				return;
			}
		}

		nbt.setInteger("x", x);
		nbt.setInteger("y", y);
		nbt.setInteger("z", z);
	}

	@Override
	public IIcon getIconIndex(ItemStack marker) {
		NBTTagCompound nbt = NBTUtils.getItemData(marker);

		if (nbt.hasKey("x")) {
			itemIcon = iconRecording;
		} else {
			itemIcon = iconBase;
		}

		return itemIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		super.registerIcons(par1IconRegister);

		iconBase = par1IconRegister.registerIcon("buildcraftbuilders:constructionMarkerBlock/default");
		iconRecording = par1IconRegister.registerIcon("buildcraftbuilders:constructionMarkerBlock/recording");
	}

	@Override
	public boolean onItemUse(ItemStack marker, EntityPlayer player, World world, int x,
							 int y, int z, int side, float par8, float par9, float par10) {

		TileEntity tile = world.getTileEntity(x, y, z);
		NBTTagCompound nbt = NBTUtils.getItemData(marker);

		if (nbt.hasKey("x")
				&& !(tile instanceof TileBuilder
				|| tile instanceof TileArchitect
				|| tile instanceof TileConstructionMarker)) {

			nbt.removeTag("x");
			nbt.removeTag("y");
			nbt.removeTag("z");

			return true;
		} else {
			return super.onItemUse(marker, player, world, x, y, z, side, par8, par9, par10);
		}
	}
}
