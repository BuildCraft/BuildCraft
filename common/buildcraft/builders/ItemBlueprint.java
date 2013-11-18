/**
 * Copyright (c) SpaceToad, 2011-2012 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import buildcraft.builders.blueprints.Blueprint;
import buildcraft.builders.blueprints.BlueprintDatabase;
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

	public ItemBlueprint(int i) {
		super(i);
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

	public static Blueprint getBlueprint(ItemStack stack) {
		NBTTagCompound nbt = NBTUtils.getItemData(stack);
		byte[] idRaw = nbt.getByteArray("blueprint");
		BlueprintId id = BlueprintId.fromRawId(idRaw);

		if (id == null) {
			return null;
		} else {
			return BlueprintDatabase.get(id);
		}
	}
}
