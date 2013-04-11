/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core.triggers;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.IAction;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class BCAction implements IAction {

	protected int id;

	public BCAction(int id) {
		this.id = id;
		ActionManager.actions[id] = this;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public abstract int getIconIndex();

	@Override
	public boolean hasParameter() {
		return false;
	}

	@Override
	public String getDescription() {
		return "";
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftCore.instance.actionTriggerIconProvider;
	}
	
	
}
