/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.triggers;

import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.TriggerParameter;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

/**
 * This class has to be implemented to create new triggers kinds to BuildCraft
 * gates. There is an instance per kind, which will get called wherever the
 * trigger can be active.
 */
public abstract class BCTrigger implements ITrigger {

	protected final int legacyId;
	protected final String uniqueTag;

	public BCTrigger(int legacyId, String uniqueTag) {
		this.legacyId = legacyId;
		this.uniqueTag = uniqueTag;
		ActionManager.registerTrigger(uniqueTag, this);
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

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public boolean isTriggerActive(ForgeDirection side, TileEntity tile, ITriggerParameter parameter) {
		return false;
	}

	@Override
	public final ITriggerParameter createParameter() {
		return new TriggerParameter();
	}
}
