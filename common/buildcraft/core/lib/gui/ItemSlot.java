/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.gui;

import net.minecraft.item.ItemStack;

public class ItemSlot extends AdvancedSlot {
	public ItemStack stack;

	public ItemSlot(GuiAdvancedInterface gui, int x, int y) {
		super(gui, x, y);
	}

	public ItemSlot(GuiAdvancedInterface gui, int x, int y, ItemStack iStack) {
		super(gui, x, y);

		stack = iStack;
	}

	@Override
	public ItemStack getItemStack() {
		return stack;
	}
}