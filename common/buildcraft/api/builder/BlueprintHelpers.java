/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.builder;

import java.util.Random;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * A collection of helpful functions to make your life easier.
 */
public class BlueprintHelpers {

	public static final Random RANDOM = new Random();

	/**
	 * Takes a schematic orientation and blueprint orientation and returns the
	 * orientation that should be used in the world. Admittedly this is not
	 * sufficient for 24-point rotation. If you need something more complex, you
	 * will have to handle it yourself.
	 */
	public static ForgeDirection rotateOrientation(ForgeDirection schematicOrientation, ForgeDirection blueprintOrientation) {
		if (schematicOrientation == ForgeDirection.UP || schematicOrientation == ForgeDirection.DOWN) {
			return schematicOrientation;
		}
		if (blueprintOrientation == ForgeDirection.SOUTH) {
			return schematicOrientation.getOpposite();
		}
		if (blueprintOrientation == ForgeDirection.WEST) {
			return schematicOrientation.getRotation(ForgeDirection.DOWN);
		}
		if (blueprintOrientation == ForgeDirection.EAST) {
			return schematicOrientation.getRotation(ForgeDirection.UP);
		}
		return schematicOrientation;
	}

	/**
	 * Takes an ItemStack and uses one. Replaces containers as needed.
	 *
	 * @return the new ItemStack
	 */
	public static ItemStack consumeItem(ItemStack stack) {
		if (stack.stackSize == 1) {
			if (stack.getItem().hasContainerItem()) {
				return stack.getItem().getContainerItem(stack);
			} else {
				return null;
			}
		} else {
			stack.splitStack(1);

			return stack;
		}
	}
}
