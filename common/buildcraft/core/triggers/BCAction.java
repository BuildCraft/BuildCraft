/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.triggers;

import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.IAction;

public abstract class BCAction implements IAction {

	protected final int legacyId;
	protected final String uniqueTag;

	public BCAction(int legacyId, String uniqueTag) {
		this.legacyId = legacyId;
		this.uniqueTag = uniqueTag;
		ActionManager.registerAction(uniqueTag, this);
	}

	@Override
	public String getUniqueTag() {
		return uniqueTag;
	}

	@Override
	public int getLegacyId() {
		return this.legacyId;
	}

	@Override
	public boolean hasParameter() {
		return false;
	}
}
