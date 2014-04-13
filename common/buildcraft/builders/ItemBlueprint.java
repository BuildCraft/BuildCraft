/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.BuildCraftBuilders;
import buildcraft.builders.blueprints.BlueprintId;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.blueprints.BlueprintBase;
import buildcraft.core.utils.NBTUtils;
import buildcraft.core.utils.StringUtils;

public abstract class ItemBlueprint extends ItemBuildCraft {

	public ItemBlueprint() {
		super(CreativeTabBuildCraft.TIER_3);
		setMaxStackSize(1);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
		if (NBTUtils.getItemData(stack).hasKey("name")) {
			String name = NBTUtils.getItemData(stack).getString("name");

			if (name.equals("")) {
				list.add(String.format(StringUtils.localize("item.blueprint.unnamed")));
			} else {
				list.add(String.format (name));
			}

			list.add(String.format(StringUtils
					.localize("item.blueprint.author")
					+ " "
					+ NBTUtils.getItemData(stack).getString("author")));
		} else {
			list.add(StringUtils.localize("item.blueprint.blank"));
		}
	}

	public static BlueprintId getId (ItemStack stack) {
		NBTTagCompound nbt = NBTUtils.getItemData(stack);
		BlueprintId id = new BlueprintId ();
		id.read (nbt);

		if (BuildCraftBuilders.serverDB.exists(id)) {
			return id;
		} else {
			return null;
		}
	}

	public static BlueprintBase loadBlueprint(ItemStack stack) {
		return BuildCraftBuilders.serverDB.load (getId (stack));
	}
}
