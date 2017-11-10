/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.builders.schematics;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;

public class SchematicRotateMeta extends SchematicTile {

	int[] rot;
	boolean rotateForward;

	int infoMask = 0;

	public SchematicRotateMeta(int[] rotations, boolean rotateForward) {
		rot = rotations;

		for (int element : rot) {
			if (element < 4) {
				infoMask = infoMask < 3 ? 3 : infoMask;
			} else if (element < 8) {
				infoMask = infoMask < 7 ? 7 : infoMask;
			} else if (element < 16) {
				infoMask = infoMask < 15 ? 15 : infoMask;
			}
		}

		this.rotateForward = rotateForward;
	}

	@Override
	public boolean isAlreadyBuilt(IBuilderContext context, int x, int y, int z) {
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
