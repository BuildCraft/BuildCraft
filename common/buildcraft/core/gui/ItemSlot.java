/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.gui;

import net.minecraft.item.ItemStack;

public class ItemSlot extends AdvancedSlot {

	public ItemStack stack;

	public ItemSlot(GuiAdvancedInterface gui, int x, int y) {
		super(gui, x, y);
	}

	@Override
	public ItemStack getItemStack() {
		return stack;
	}
}