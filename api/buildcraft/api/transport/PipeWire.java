/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.transport;

import java.util.Locale;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public enum PipeWire {

	RED, BLUE, GREEN, YELLOW;
	public static Item item;
	public static final PipeWire[] VALUES = values();

	public PipeWire reverse() {
		switch (this) {
			case RED:
				return YELLOW;
			case BLUE:
				return GREEN;
			case GREEN:
				return BLUE;
			default:
				return RED;
		}
	}

	public String getTag() {
		return name().toLowerCase(Locale.ENGLISH) + "PipeWire";
	}

	public String getColor() {
		String name = this.toString().toLowerCase(Locale.ENGLISH);
		char first = Character.toUpperCase(name.charAt(0));
		return first + name.substring(1);
	}

	public ItemStack getStack() {
		return getStack(1);
	}

	public ItemStack getStack(int qty) {
		if (item == null) {
			return null;
		} else {
			return new ItemStack(item, qty, ordinal());
		}
	}

	public boolean isPipeWire(ItemStack stack) {
		if (stack == null) {
			return false;
		} else if (stack.getItem() != item) {
			return false;
		} else {
			return stack.getItemDamage() == ordinal();
		}
	}

	public static PipeWire fromOrdinal(int ordinal) {
		if (ordinal < 0 || ordinal >= VALUES.length) {
			return RED;
		} else {
			return VALUES[ordinal];
		}
	}
}
