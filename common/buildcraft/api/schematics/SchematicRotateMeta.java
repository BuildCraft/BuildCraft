/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.schematics;

import java.util.LinkedList;

import net.minecraft.item.ItemStack;
import buildcraft.api.blueprints.Schematic;
import buildcraft.api.blueprints.IBuilderContext;

public class SchematicRotateMeta extends Schematic {

	int[] rot;
	boolean rotateForward;

	int infoMask = 0;

	public SchematicRotateMeta(int[] rotations, boolean rotateForward) {
		rot = rotations;

		for (int element : rot) {
			if (element < 4) {
				infoMask = (infoMask < 3 ? 3 : infoMask);
			} else if (element < 8) {
				infoMask = (infoMask < 7 ? 7 : infoMask);
			} else if (element < 16) {
				infoMask = (infoMask < 15 ? 15 : infoMask);
			}
		}

		this.rotateForward = rotateForward;
	}

	@Override
	public void addRequirements(IBuilderContext context, LinkedList<ItemStack> requirements) {
		requirements.add(new ItemStack(block, 1, 0));
	}

	@Override
	public boolean isValid(IBuilderContext context) {
		return block == context.world().getBlock(x, y, z);
	}

	@Override
	public void rotateLeft(IBuilderContext context) {
		int pos = meta & infoMask;
		int others = meta - pos;

		if (rotateForward) {
			if (pos == rot[0]) {
				pos = rot[1];
			} else if (pos == rot[1]) {
				pos = rot[2];
			} else if (pos == rot[2]) {
				pos = rot[3];
			} else if (pos == rot[3]) {
				pos = rot[0];
			}
		} else {
			if (pos == rot[0]) {
				pos = rot[3];
			} else if (pos == rot[1]) {
				pos = rot[2];
			} else if (pos == rot[2]) {
				pos = rot[0];
			} else if (pos == rot[3]) {
				pos = rot[1];
			}
		}

		meta = pos + others;
	}

}
