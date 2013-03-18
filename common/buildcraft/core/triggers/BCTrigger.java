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
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.TriggerParameter;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;

/**
 * This class has to be implemented to create new triggers kinds to BuildCraft gates. There is an instance per kind, which will get called wherever the trigger
 * can be active.
 */
public abstract class BCTrigger implements ITrigger {

	protected int id;

	public BCTrigger(int id) {
		this.id = id;
		ActionManager.triggers[id] = this;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
    @SideOnly(Side.CLIENT)
	public abstract Icon getTextureIcon();
    
    @Override
    @SideOnly(Side.CLIENT)
    public IIconProvider getIconProvider() {
    	return BuildCraftCore.instance.actionTriggerIconProvider;
    }

	@Override
	public boolean hasParameter() {
		return false;
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public boolean isTriggerActive(TileEntity tile, ITriggerParameter parameter) {
		return false;
	}

	@Override
	public final ITriggerParameter createParameter() {
		return new TriggerParameter();
	}
}
