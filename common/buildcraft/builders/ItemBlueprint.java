/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import buildcraft.BuildCraftBuilders;
import buildcraft.api.blueprints.BuildingPermission;
import buildcraft.api.items.IBlueprintItem;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.blueprints.Blueprint;
import buildcraft.core.blueprints.BlueprintBase;
import buildcraft.core.blueprints.LibraryId;
import buildcraft.core.blueprints.Template;
import buildcraft.core.lib.items.ItemBuildCraft;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.core.lib.utils.StringUtils;

public abstract class ItemBlueprint extends ItemBuildCraft implements IBlueprintItem {
	public ItemBlueprint() {
		super(BCCreativeTab.get("main"));
	}

	@Override
	public String getName(ItemStack stack) {
		return NBTUtils.getItemData(stack).getString("name");
	}

	@Override
	public boolean setName(ItemStack stack, String name) {
		NBTUtils.getItemData(stack).setString("name", name);
		return true;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
		if (NBTUtils.getItemData(stack).hasKey("name")) {
			String name = NBTUtils.getItemData(stack).getString("name");

			if ("".equals(name)) {
				list.add(StringUtils.localize("item.blueprint.unnamed"));
			} else {
				list.add(name);
			}

			list.add(StringUtils.localize("item.blueprint.author")
					+ " "
					+ NBTUtils.getItemData(stack).getString("author"));
		} else {
			list.add(StringUtils.localize("item.blueprint.blank"));
		}

		if (NBTUtils.getItemData(stack).hasKey("permission")) {
			BuildingPermission p = BuildingPermission.values()[NBTUtils.getItemData(stack).getByte("permission")];

			if (p == BuildingPermission.CREATIVE_ONLY) {
				list.add(StringUtils.localize("item.blueprint.creative_only"));
			} else if (p == BuildingPermission.NONE) {
				list.add(StringUtils.localize("item.blueprint.no_build"));
			}
		}

		if (NBTUtils.getItemData(stack).hasKey("isComplete")) {
			boolean isComplete = NBTUtils.getItemData(stack).getBoolean("isComplete");

			if (!isComplete) {
				list.add(StringUtils.localize("item.blueprint.incomplete"));
			}
		}
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return NBTUtils.getItemData(stack).hasKey("name") ? 1 : 16;
	}

	public abstract String getIconType();

	@Override
	public String[] getIconNames() {
		return new String[]{getIconType() + "/clean", getIconType() + "/used"};
	}

	@Override
	public IIcon getIconIndex(ItemStack stack) {
		if (!NBTUtils.getItemData(stack).hasKey("name")) {
			itemIcon = icons[0];
		} else {
			itemIcon = icons[1];
		}

		return itemIcon;
	}

	public static boolean isContentReadable(ItemStack stack) {
		return getId(stack) != null;
	}

	public static LibraryId getId(ItemStack stack) {
		NBTTagCompound nbt = NBTUtils.getItemData(stack);
		if (nbt == null) {
			return null;
		}
		LibraryId id = new LibraryId();
		id.read(nbt);

		if (BuildCraftBuilders.serverDB.exists(id)) {
			return id;
		} else {
			return null;
		}
	}

	public static BlueprintBase loadBlueprint(ItemStack stack) {
		if (stack == null || stack.getItem() == null || !(stack.getItem() instanceof IBlueprintItem)) {
			return null;
		}

		LibraryId id = getId(stack);
		if (id == null) {
			return null;
		}

		NBTTagCompound nbt = BuildCraftBuilders.serverDB.load(id);
		BlueprintBase base;
		if (((IBlueprintItem) stack.getItem()).getType(stack) == IBlueprintItem.Type.TEMPLATE) {
			base = new Template();
		} else {
			base = new Blueprint();
		}
		base.readFromNBT(nbt);
		base.id = id;
		return base;
	}
}
