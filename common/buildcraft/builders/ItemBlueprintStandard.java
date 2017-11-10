/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import net.minecraft.item.ItemStack;

public class ItemBlueprintStandard extends ItemBlueprint {
	public ItemBlueprintStandard() {
		super();
	}

	@Override
	public String getIconType() {
		return "blueprint";
	}

	@Override
	public Type getType(ItemStack stack) {
		return Type.BLUEPRINT;
	}
}
