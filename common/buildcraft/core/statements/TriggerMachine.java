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
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.tiles.IHasWork;
import buildcraft.core.lib.utils.StringUtils;

public class TriggerMachine extends BCStatement implements ITriggerExternal {

	boolean active;

	public TriggerMachine(boolean active) {
		super("buildcraft:work." + (active ? "scheduled" : "done"), "buildcraft.work." + (active ? "scheduled" : "done"));

		this.active = active;
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.trigger.machine." + (active ? "scheduled" : "done"));
	}

	@Override
	public boolean isTriggerActive(TileEntity tile, ForgeDirection side, IStatementContainer container, IStatementParameter[] parameters) {
		if (tile instanceof IHasWork) {
			IHasWork machine = (IHasWork) tile;

			if (active) {
				return machine.hasWork();
			} else {
				return !machine.hasWork();
			}
		}

		return false;
	}

	@Override
	public void registerIcons(IIconRegister register) {
		icon = register.registerIcon("buildcraftcore:triggers/trigger_machine_" + (active ? "active" : "inactive"));
	}
}
