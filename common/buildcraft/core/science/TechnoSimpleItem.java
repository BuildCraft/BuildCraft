/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.science;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TechnoSimpleItem extends Technology {

	private ItemStack itemToDisplay;

	public void initialize(Tier iTier,
			Object iItemToDisplay,
			ItemStack requirement,
			Technology... iPrerequisites) {
		initialize(iTier, iItemToDisplay, requirement, null, null, iPrerequisites);
	}

	public void initialize(Tier iTier,
			Object iItemToDisplay,
			ItemStack requirement1,
			ItemStack requirement2,
			Technology... iPrerequisites) {
		initialize(iTier, iItemToDisplay, requirement1, requirement2, null, iPrerequisites);
	}

	public void initialize(Tier iTier,
			Object iItemToDisplay,
			ItemStack requirement1,
			ItemStack requirement2,
			ItemStack requirement3,
			Technology... iPrerequisites) {

		super.initialize("item:" + toStack(iItemToDisplay).getUnlocalizedName(),
				iTier, requirement1, requirement2, requirement3, iPrerequisites);

		itemToDisplay = toStack(iItemToDisplay);
	}

	@Override
	public ItemStack getStackToDisplay() {
		return itemToDisplay;
	}

	@Override
	public String getLocalizedName() {
		return "";
	}

	public static ItemStack toStack(Object obj) {
		if (obj instanceof ItemStack) {
			return (ItemStack) obj;
		} else if (obj instanceof Item) {
			return new ItemStack((Item) obj);
		} else if (obj instanceof Block) {
			return new ItemStack((Block) obj);
		} else {
			return null;
		}
	}
}
