/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core.triggers;

import buildcraft.api.gates.Action;
import buildcraft.core.DefaultProps;

public class ActionRedstoneOutput extends Action {

	public ActionRedstoneOutput(int id) {
		super(id);
	}

	@Override
	public int getIndexInTexture() {
		return 0 * 16 + 0;
	}

	@Override
	public String getDescription() {
		return "Redstone Signal";
	}

	@Override
	public String getTexture() {
		return DefaultProps.TEXTURE_TRIGGERS;
	}
}
