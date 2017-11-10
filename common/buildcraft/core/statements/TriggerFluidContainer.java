/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.statements;

import java.util.Locale;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.core.lib.utils.StringUtils;

public class TriggerFluidContainer extends BCStatement implements ITriggerExternal {

	public enum State {

		Empty, Contains, Space, Full
	}

	public State state;

	public TriggerFluidContainer(State state) {
		super("buildcraft:fluid." + state.name().toLowerCase(Locale.ENGLISH), "buildcraft.fluid." + state.name().toLowerCase(Locale.ENGLISH));
		this.state = state;
	}

	@Override
	public int maxParameters() {
		return state == State.Contains || state == State.Space ? 1 : 0;
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.trigger.fluid." + state.name().toLowerCase(Locale.ENGLISH));
	}

	@Override
	public boolean isTriggerActive(TileEntity tile, ForgeDirection side, IStatementContainer statementContainer, IStatementParameter[] parameters) {
		if (tile instanceof IFluidHandler) {
			IFluidHandler container = (IFluidHandler) tile;

			FluidStack searchedFluid = null;

			if (parameters != null && parameters.length >= 1 && parameters[0] != null && parameters[0].getItemStack() != null) {
				searchedFluid = FluidContainerRegistry.getFluidForFilledItem(parameters[0].getItemStack());
			}

			if (searchedFluid != null) {
				searchedFluid.amount = 1;
			}

			FluidTankInfo[] liquids = container.getTankInfo(side);
			if (liquids == null || liquids.length == 0) {
				return false;
			}

			switch (state) {
				case Empty:
					for (FluidTankInfo c : liquids) {
						if (c != null && c.fluid != null && c.fluid.amount > 0 && (searchedFluid == null || searchedFluid.isFluidEqual(c.fluid))) {
							return false;
						}
					}
					return true;
				case Contains:
					for (FluidTankInfo c : liquids) {
						if (c != null && c.fluid != null && c.fluid.amount > 0 && (searchedFluid == null || searchedFluid.isFluidEqual(c.fluid))) {
							return true;
						}
					}
					return false;
				case Space:
					if (searchedFluid == null) {
						for (FluidTankInfo c : liquids) {
							if (c != null && (c.fluid == null || c.fluid.amount < c.capacity)) {
								return true;
							}
						}
						return false;
					}
					return container.fill(side, searchedFluid, false) > 0;
				case Full:
					if (searchedFluid == null) {
						for (FluidTankInfo c : liquids) {
							if (c != null && (c.fluid == null || c.fluid.amount < c.capacity)) {
								return false;
							}
						}
						return true;
					}
					return container.fill(side, searchedFluid, false) <= 0;
			}
		}

		return false;
	}

	@Override
	public void registerIcons(IIconRegister register) {
		icon = register.registerIcon("buildcraftcore:triggers/trigger_liquidcontainer_" + state.name().toLowerCase());
	}

	@Override
	public IStatementParameter createParameter(int index) {
		return new StatementParameterItemStack();
	}
}
