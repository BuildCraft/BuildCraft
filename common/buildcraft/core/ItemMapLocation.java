/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.IBox;
import buildcraft.api.core.IPathProvider;
import buildcraft.api.core.IZone;
import buildcraft.api.items.IMapLocation;
import buildcraft.core.lib.items.ItemBuildCraft;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.robotics.ZonePlan;

public class ItemMapLocation extends ItemBuildCraft implements IMapLocation {
	public ItemMapLocation() {
		super(BCCreativeTab.get("main"));
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return NBTUtils.getItemData(stack).hasKey("kind") ? 1 : 16;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
		NBTTagCompound cpt = NBTUtils.getItemData(stack);

		if (cpt.hasKey("name")) {
			String name = cpt.getString("name");
			if (name.length() > 0) {
				list.add(name);
			}
		}

		if (cpt.hasKey("kind")) {
			switch (cpt.getByte("kind")) {
				case 0: {
					int x = cpt.getInteger("x");
					int y = cpt.getInteger("y");
					int z = cpt.getInteger("z");
					ForgeDirection side = ForgeDirection.values()[cpt.getByte("side")];

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
					BlockIndex first = new BlockIndex(pathNBT.getCompoundTagAt(0));

					int x = first.x;
					int y = first.y;
					int z = first.z;

					list.add(StringUtils.localize("{" + x + ", " + y + ", " + z + "} + " + pathNBT.tagCount() + " elements"));
					break;
				}
				case 3: {
					break;
				}
			}
		}
	}

	@Override
	public IIcon getIconIndex(ItemStack stack) {
		NBTTagCompound cpt = NBTUtils.getItemData(stack);

		if (!cpt.hasKey("kind")) {
			return icons[0];
		} else {
			return getIconFromDamage(cpt.getByte("kind") + 1);
		}
	}

	@Override
	public String[] getIconNames() {
		return new String[]{"mapLocation/clean", "mapLocation/spot", "mapLocation/area", "mapLocation/path", "mapLocation/zone"};
	}


	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		super.registerIcons(par1IconRegister);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer par2EntityPlayer, World world, int x,
							 int y, int z, int side, float par8, float par9, float par10) {
		TileEntity tile = world.getTileEntity(x, y, z);
		NBTTagCompound cpt = NBTUtils.getItemData(stack);

		if (tile instanceof IPathProvider) {
			cpt.setByte("kind", (byte) 2);

			NBTTagList pathNBT = new NBTTagList();

			for (BlockIndex index : ((IPathProvider) tile).getPath()) {
				NBTTagCompound nbt = new NBTTagCompound();
				index.writeTo(nbt);
				pathNBT.appendTag(nbt);
			}

			cpt.setTag("path", pathNBT);
		} else if (tile instanceof IAreaProvider) {
			cpt.setByte("kind", (byte) 1);

			IAreaProvider areaTile = (IAreaProvider) tile;

			cpt.setInteger("xMin", areaTile.xMin());
			cpt.setInteger("yMin", areaTile.yMin());
			cpt.setInteger("zMin", areaTile.zMin());
			cpt.setInteger("xMax", areaTile.xMax());
			cpt.setInteger("yMax", areaTile.yMax());
			cpt.setInteger("zMax", areaTile.zMax());

		} else {
			cpt.setByte("kind", (byte) 0);

			cpt.setByte("side", (byte) side);
			cpt.setInteger("x", x);
			cpt.setInteger("y", y);
			cpt.setInteger("z", z);
		}

		return true;
	}

	@Override
	public IBox getBox(ItemStack item) {
		NBTTagCompound cpt = NBTUtils.getItemData(item);

		if (cpt.hasKey("kind") && cpt.getByte("kind") == 1) {
			int xMin = cpt.getInteger("xMin");
			int yMin = cpt.getInteger("yMin");
			int zMin = cpt.getInteger("zMin");
			int xMax = cpt.getInteger("xMax");
			int yMax = cpt.getInteger("yMax");
			int zMax = cpt.getInteger("zMax");

			return new Box(xMin, yMin, zMin, xMax, yMax, zMax);
		} else if (cpt.hasKey("kind") && cpt.getByte("kind") == 0) {
			return getPointBox(item);
		} else {
			return null;
		}
	}

	public static IBox getPointBox(ItemStack item) {
		NBTTagCompound cpt = NBTUtils.getItemData(item);

		if (cpt.hasKey("kind") && cpt.getByte("kind") == 0) {
			int x = cpt.getInteger("x");
			int y = cpt.getInteger("y");
			int z = cpt.getInteger("z");

			return new Box(x, y, z, x, y, z);
		} else {
			return null;
		}
	}

	@Override
	public ForgeDirection getPointSide(ItemStack item) {
		NBTTagCompound cpt = NBTUtils.getItemData(item);

		if (cpt.hasKey("kind") && cpt.getByte("kind") == 0) {
			return ForgeDirection.values()[cpt.getByte("side")];
		} else {
			return ForgeDirection.UNKNOWN;
		}
	}

	@Override
	public BlockIndex getPoint(ItemStack item) {
		NBTTagCompound cpt = NBTUtils.getItemData(item);

		if (cpt.hasKey("kind") && cpt.getByte("kind") == 0) {
			return new BlockIndex(cpt.getInteger("x"), cpt.getInteger("y"), cpt.getInteger("z"));
		} else {
			return null;
		}
	}

	@Override
	public IZone getZone(ItemStack item) {
		NBTTagCompound cpt = NBTUtils.getItemData(item);

		if (cpt.hasKey("kind") && cpt.getByte("kind") == 3) {
			ZonePlan plan = new ZonePlan();
			plan.readFromNBT(cpt);

			return plan;
		} else if (cpt.hasKey("kind") && cpt.getByte("kind") == 1) {
			return getBox(item);
		} else if (cpt.hasKey("kind") && cpt.getByte("kind") == 0) {
			return getPointBox(item);
		} else {
			return null;
		}
	}

	@Override
	public List<BlockIndex> getPath(ItemStack item) {
		NBTTagCompound cpt = NBTUtils.getItemData(item);

		if (cpt.hasKey("kind") && cpt.getByte("kind") == 2) {
			List<BlockIndex> indexList = new ArrayList<BlockIndex>();
			NBTTagList pathNBT = cpt.getTagList("path", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < pathNBT.tagCount(); i++) {
				indexList.add(new BlockIndex(pathNBT.getCompoundTagAt(i)));
			}
			return indexList;
		} else if (cpt.hasKey("kind") && cpt.getByte("kind") == 0) {
			List<BlockIndex> indexList = new ArrayList<BlockIndex>();
			indexList.add(new BlockIndex(cpt.getInteger("x"), cpt.getInteger("y"), cpt.getInteger("z")));
			return indexList;
		} else {
			return null;
		}
	}

	public static void setZone(ItemStack item, ZonePlan plan) {
		NBTTagCompound cpt = NBTUtils.getItemData(item);

		cpt.setByte("kind", (byte) 3);
		plan.writeToNBT(cpt);
	}

	@Override
	public String getName(ItemStack item) {
		return NBTUtils.getItemData(item).getString("name");
	}

	@Override
	public boolean setName(ItemStack item, String name) {
		NBTTagCompound cpt = NBTUtils.getItemData(item);
		cpt.setString("name", name);

		return true;
	}

	@Override
	public MapLocationType getType(ItemStack stack) {
		NBTTagCompound cpt = NBTUtils.getItemData(stack);
		return MapLocationType.values()[cpt.getByte("kind")];
	}
}
