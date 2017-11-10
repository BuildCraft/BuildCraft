/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.BuildCraftCore;
import buildcraft.api.items.IList;
import buildcraft.core.lib.items.ItemBuildCraft;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.core.list.ListHandlerNew;
import buildcraft.core.list.ListHandlerOld;

public class ItemList extends ItemBuildCraft implements IList {
	public ItemList() {
		super();
		setHasSubtypes(true);
		setMaxStackSize(1);
	}

	@Override
	public IIcon getIconIndex(ItemStack stack) {
		itemIcon = icons[NBTUtils.getItemData(stack).hasKey("written") ? 1 : 0];
		return itemIcon;
	}

	@Override
	public String[] getIconNames() {
		return new String[]{"list/clean", "list/used"};
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (!world.isRemote) {
			player.openGui(BuildCraftCore.instance, stack.getItemDamage() == 1 ? GuiIds.LIST_NEW : GuiIds.LIST_OLD, world, 0, 0, 0);
		}

		return stack;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
		if (stack.getItemDamage() == 0) {
			list.add(EnumChatFormatting.DARK_RED + StatCollector.translateToLocal("tip.deprecated"));
		}

		NBTTagCompound nbt = NBTUtils.getItemData(stack);
		if (nbt.hasKey("label")) {
			list.add(nbt.getString("label"));
		}
	}

	public static void saveLabel(ItemStack stack, String text) {
		NBTTagCompound nbt = NBTUtils.getItemData(stack);

		nbt.setString("label", text);
	}

	@Override
	public boolean setName(ItemStack stack, String name) {
		saveLabel(stack, name);
		return true;
	}

	@Override
	public String getName(ItemStack stack) {
		return getLabel(stack);
	}

	@Override
	public String getLabel(ItemStack stack) {
		return NBTUtils.getItemData(stack).getString("label");
	}

	@Override
	public boolean matches(ItemStack stackList, ItemStack item) {
		if (stackList.getItemDamage() == 1) {
			return ListHandlerNew.matches(stackList, item);
		} else {
			return ListHandlerOld.matches(stackList, item);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List itemList) {
		itemList.add(new ItemStack(this, 1, 0)); // TODO: remove
		itemList.add(new ItemStack(this, 1, 1));
	}
}
