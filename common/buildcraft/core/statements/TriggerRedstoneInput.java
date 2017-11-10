/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.statements;

import net.minecraft.client.renderer.texture.IIconRegister;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.containers.IRedstoneStatementContainer;
import buildcraft.api.statements.containers.ISidedStatementContainer;
import buildcraft.core.lib.utils.StringUtils;

public class TriggerRedstoneInput extends BCStatement implements ITriggerInternal {

	boolean active;

	public TriggerRedstoneInput(boolean active) {
		super("buildcraft:redstone.input." + (active ? "active" : "inactive"), active ? "buildcraft.redtone.input.active" : "buildcraft.redtone.input.inactive");
		this.active = active;
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.trigger.redstone.input." + (active ? "active" : "inactive"));
	}

	@Override
	public IStatementParameter createParameter(int index) {
		IStatementParameter param = null;

		if (index == 0) {
			param = new StatementParameterRedstoneGateSideOnly();
		}

		return param;
	}

	@Override
	public int maxParameters() {
		return 1;
	}

	@Override
	public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
		if (container instanceof IRedstoneStatementContainer) {
			int level = ((IRedstoneStatementContainer) container).getRedstoneInput(ForgeDirection.UNKNOWN);
			if (parameters.length > 0 && parameters[0] instanceof StatementParameterRedstoneGateSideOnly &&
					((StatementParameterRedstoneGateSideOnly) parameters[0]).isOn &&
					container instanceof ISidedStatementContainer) {
				level = ((IRedstoneStatementContainer) container).getRedstoneInput(((ISidedStatementContainer) container).getSide());
			}

			return active ? level > 0 : level == 0;
		} else {
			return false;
		}
	}

	@Override
	public void registerIcons(IIconRegister register) {
		icon = register.registerIcon("buildcraftcore:triggers/trigger_redstoneinput_" + (active ? "active" : "inactive"));
	}
}
