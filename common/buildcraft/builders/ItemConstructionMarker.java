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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import buildcraft.api.core.Position;
import buildcraft.core.utils.NBTUtils;

public class ItemConstructionMarker extends ItemBlock {

	/*@SideOnly(Side.CLIENT)
	public IIcon iconBase;

	@SideOnly(Side.CLIENT)
	public IIcon iconRecording;*/

	public ItemConstructionMarker(Block block) {
		super(block);
	}

	public static boolean linkStarted(ItemStack marker) {
		return NBTUtils.getItemData(marker).hasKey("x");
	}

	public static void link(ItemStack marker, World world, BlockPos pos) {
		NBTTagCompound nbt = NBTUtils.getItemData(marker);

		if (nbt.hasKey("x")) {
			int ox = nbt.getInteger("x");
			int oy = nbt.getInteger("y");
			int oz = nbt.getInteger("z");

			TileEntity tile1 = world.getTileEntity(new BlockPos(ox, oy, oz));

			if (!new Position(ox, oy, oz).isClose(new Position(pos), 64)) {
				return;
			}

			if (tile1 != null && (tile1 instanceof TileArchitect)) {
				TileArchitect architect = (TileArchitect) tile1;
				TileEntity tile2 = world.getTileEntity(pos);

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

		nbt.setInteger("x", pos.getX());
		nbt.setInteger("y", pos.getY());
		nbt.setInteger("z", pos.getZ());
	}

	/*@Override
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

		iconBase = par1IconRegister.registerIcon("buildcraft:constructMarker");
		iconRecording = par1IconRegister.registerIcon("buildcraft:constructMarkerRec");
	}*/

	@Override
	public boolean onItemUse(ItemStack marker, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {

		TileEntity tile = world.getTileEntity(pos);
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
			return super.onItemUse(marker, player, world, pos, side, hitX, hitY, hitZ);
		}
	}
}
