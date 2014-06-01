/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.boards;

import buildcraft.api.boards.IBoardParameter;

public abstract class BoardParameter implements IBoardParameter {

	private String name = "<unnamed>";

	@Override
	public final String getName () {
		return name;
	}

	public void setName(String iName) {
		name = iName;
	}

}
