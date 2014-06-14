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

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.core.utils.NBTUtils;
import buildcraft.core.utils.StringUtils;

public class ItemLocation extends ItemBuildCraft {

	public IIcon cleanLocation;
	public IIcon usedLocation;

	public ItemLocation() {
		super(CreativeTabBuildCraft.ITEMS);
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return NBTUtils.getItemData(stack).hasKey("x") ? 1 : 16;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
		NBTTagCompound cpt = NBTUtils.getItemData(stack);

		if (cpt.hasKey("x")) {
			int x = cpt.getInteger("x");
			int y = cpt.getInteger("y");
			int z = cpt.getInteger("z");
			ForgeDirection side = ForgeDirection.values()[cpt.getByte("side")];

			list.add(StringUtils.localize("{" + x + ", " + y + ", " + z + ", " + side + "}"));
		}
	}

	@Override
	public IIcon getIconIndex(ItemStack stack) {
		NBTTagCompound cpt = NBTUtils.getItemData(stack);

		if (!cpt.hasKey("x")) {
			itemIcon = cleanLocation;
		} else {
			itemIcon = usedLocation;
		}

		return itemIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		cleanLocation = par1IconRegister.registerIcon("buildcraft:location_clean");
		usedLocation = par1IconRegister.registerIcon("buildcraft:location_used");

		RedstoneBoardRegistry.instance.registerIcons(par1IconRegister);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer par2EntityPlayer, World par3World, int x,
			int y, int z, int side, float par8, float par9, float par10) {

		NBTTagCompound cpt = NBTUtils.getItemData(stack);

		cpt.setInteger("x", x);
		cpt.setInteger("y", y);
		cpt.setInteger("z", z);
		cpt.setByte("side", (byte) side);

		return true;
	}

}
