/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.inventory.filters;

import net.minecraft.item.ItemStack;

/**
 * This interface provides a convenient means of dealing with entire classes of
 * items without having to specify each item individually.
 */
public interface IStackFilter {

	public boolean matches(ItemStack stack);
}
