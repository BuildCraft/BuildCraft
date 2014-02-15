/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import buildcraft.BuildCraftBuilders;
import buildcraft.builders.blueprints.Blueprint;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.utils.NBTUtils;
import buildcraft.core.utils.StringUtils;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.builders.blueprints.BlueprintId;

public abstract class ItemBlueprint extends ItemBuildCraft {

	public ItemBlueprint() {
		super();
		setMaxStackSize(1);
		setCreativeTab(CreativeTabBuildCraft.MACHINES.get());
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
		// TODO: This code will break in SMP, put name and creator in NBT
		Blueprint blueprint = getBlueprint(stack);
		if (blueprint != null) {
			list.add(String.format(StringUtils.localize("item.blueprint.name"), blueprint.getName()));
			list.add(String.format(StringUtils.localize("item.blueprint.creator"), blueprint.getCreator()));
		} else
			list.add(StringUtils.localize("item.blueprint.blank"));
	}

	public static ItemStack getBlueprintItem(Blueprint blueprint) {
		ItemStack stack = new ItemStack(BuildCraftBuilders.blueprintItem, 1, 1);
		NBTTagCompound nbt = NBTUtils.getItemData(stack);
		blueprint.getId().write (nbt);
		return stack;
	}


	public static Blueprint getBlueprint(ItemStack stack) {
		if (stack == null) {
			return null;
		}

		NBTTagCompound nbt = NBTUtils.getItemData(stack);
		BlueprintId id = new BlueprintId ();

		id.read (nbt);

		return BuildCraftBuilders.serverDB.get(id);
	}
}
