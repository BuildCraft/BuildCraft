/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.Constants;
import net.minecraft.util.EnumFacing;
import buildcraft.api.boards.RedstoneBoardRegistry;
import net.minecraft.util.BlockPos;
import buildcraft.api.core.IBox;
import buildcraft.api.core.IZone;
import buildcraft.builders.TileMarker;
import buildcraft.builders.TilePathMarker;
import buildcraft.core.utils.ModelHelper;
import buildcraft.core.utils.NBTUtils;
import buildcraft.core.utils.StringUtils;
import buildcraft.core.utils.Utils;

public class ItemMapLocation extends ItemBuildCraft {

	/*public IIcon clean;
	public IIcon spot;
	public IIcon area;
	public IIcon path;
	public IIcon zone;*/

	public ItemMapLocation() {
		super(CreativeTabBuildCraft.ITEMS);
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return NBTUtils.getItemData(stack).hasKey("kind") ? 1 : 16;
	}

	@Override
	public void registerModels() {
		ModelHelper.registerItemModel(this, 0, "");
		ModelHelper.registerItemModel(this, 1, "Spot");
		ModelHelper.registerItemModel(this, 2, "Area");
		ModelHelper.registerItemModel(this, 3, "Path");
		ModelHelper.registerItemModel(this, 4, "Zone");
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
		NBTTagCompound cpt = NBTUtils.getItemData(stack);

		if (!cpt.hasKey("kind")) {
		} else {
			switch (cpt.getByte("kind")) {
			case 0: {
				int x = cpt.getInteger("x");
				int y = cpt.getInteger("y");
				int z = cpt.getInteger("z");
				EnumFacing side = EnumFacing.values()[cpt.getByte("side")];

				list.add(StringUtils.localize("{" + x + ", " + y + ", " + z + ", " + side + "}"));
				break;
			}
			case 1: {
				int x = cpt.getInteger("xMin");
				int y = cpt.getInteger("yMin");
				int z = cpt.getInteger("zMin");
				int xLength = cpt.getInteger("xMax") - x + 1;
				int yLength = cpt.getInteger("yMax") - y + 1;
				int zLength = cpt.getInteger("zMax") - z + 1;

				list.add(StringUtils.localize("{" + x + ", " + y + ", " + z + "} + {" + xLength + " x " + yLength + " x " + zLength + "}"));
				break;
			}
			case 2: {
				NBTTagList pathNBT = cpt.getTagList("path", Constants.NBT.TAG_COMPOUND);
				BlockPos first = Utils.readBlockPos(pathNBT.getCompoundTagAt(0));

				int x = first.getX();
				int y = first.getY();
				int z = first.getZ();

				list.add(StringUtils.localize("{" + x + ", " + y + ", " + z + "} + " + pathNBT.tagCount() + " elements"));
				break;
			}
			case 3: {
				break;
			}
			}
		}

		if (cpt.hasKey("kind")) {
		}
	}

	/*@Override
	public IIcon getIconIndex(ItemStack stack) {
		NBTTagCompound cpt = NBTUtils.getItemData(stack);

		if (!cpt.hasKey("kind")) {
			itemIcon = clean;
		} else {
			switch (cpt.getByte("kind")) {
			case 0:
				itemIcon = spot;
				break;
			case 1:
				itemIcon = area;
				break;
			case 2:
				itemIcon = path;
				break;
			case 3:
				itemIcon = zone;
				break;
			}
		}

		return itemIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		clean = par1IconRegister.registerIcon("buildcraft:map_clean");
		spot = par1IconRegister.registerIcon("buildcraft:map_spot");
		area = par1IconRegister.registerIcon("buildcraft:map_area");
		path = par1IconRegister.registerIcon("buildcraft:map_path");
		zone = par1IconRegister.registerIcon("buildcraft:map_zone");

		RedstoneBoardRegistry.instance.registerIcons(par1IconRegister);
	}*/

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity tile = world.getTileEntity(pos);
		NBTTagCompound cpt = NBTUtils.getItemData(stack);

		if (tile instanceof TilePathMarker) {
			cpt.setByte("kind", (byte) 2);
			stack.setItemDamage(3);

			TilePathMarker pathTile = (TilePathMarker) tile;

			NBTTagList pathNBT = new NBTTagList();

			for (BlockPos index : pathTile.getPath()) {
				NBTTagCompound nbt = new NBTTagCompound();
				Utils.writeBlockPos(nbt, index);
				pathNBT.appendTag(nbt);
			}

			cpt.setTag("path", pathNBT);
		} else if (tile instanceof TileMarker) {
			cpt.setByte("kind", (byte) 1);
			stack.setItemDamage(2);

			TileMarker areaTile = (TileMarker) tile;

			cpt.setInteger("xMin", areaTile.xMin());
			cpt.setInteger("yMin", areaTile.yMin());
			cpt.setInteger("zMin", areaTile.zMin());
			cpt.setInteger("xMax", areaTile.xMax());
			cpt.setInteger("yMax", areaTile.yMax());
			cpt.setInteger("zMax", areaTile.zMax());

		} else {
			cpt.setByte("kind", (byte) 0);
			stack.setItemDamage(1);

			cpt.setByte("side", (byte) side.getIndex());
			
			Utils.writeBlockPos(cpt, pos);
		}

		return true;
	}

	public static BlockPos getBlockPos(ItemStack item) {
		NBTTagCompound cpt = NBTUtils.getItemData(item);

		if (cpt.hasKey("kind") && cpt.getByte("kind") == 0) {
			int x = cpt.getInteger("x");
			int y = cpt.getInteger("y");
			int z = cpt.getInteger("z");

			return new BlockPos(x, y, z);
		} else {
			return null;
		}
	}

	public static IBox getBox(ItemStack item) {
		NBTTagCompound cpt = NBTUtils.getItemData(item);

		if (cpt.hasKey("kind") && cpt.getByte("kind") == 1) {
			int xMin = cpt.getInteger("xMin");
			int yMin = cpt.getInteger("yMin");
			int zMin = cpt.getInteger("zMin");
			int xMax = cpt.getInteger("xMax");
			int yMax = cpt.getInteger("yMax");
			int zMax = cpt.getInteger("zMax");

			return new Box(xMin, yMin, zMin, xMax, yMax, zMax);
		} else {
			return null;
		}
	}

	public static EnumFacing getSide(ItemStack item) {
		NBTTagCompound cpt = NBTUtils.getItemData(item);

		if (cpt.hasKey("kind") && cpt.getByte("kind") == 0) {
			return EnumFacing.values()[cpt.getByte("side")];
		} else {
			return null;
		}
	}

	public static IZone getZone(ItemStack item) {
		NBTTagCompound cpt = NBTUtils.getItemData(item);

		if (cpt.hasKey("kind") && cpt.getByte("kind") == 3) {
			ZonePlan plan = new ZonePlan();
			plan.readFromNBT(cpt);

			return plan;
		} else if (cpt.hasKey("kind") && cpt.getByte("kind") == 1) {
			return getBox(item);
		} else {
			return null;
		}
	}

	public static void setZone(ItemStack item, ZonePlan plan) {
		NBTTagCompound cpt = NBTUtils.getItemData(item);

		cpt.setByte("kind", (byte) 3);
		item.setItemDamage(4);
		plan.writeToNBT(cpt);
	}
}
