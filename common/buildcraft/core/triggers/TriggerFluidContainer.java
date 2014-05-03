/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.triggers;

import java.util.Locale;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.api.gates.ITileTrigger;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.utils.StringUtils;

public class TriggerFluidContainer extends BCTrigger implements ITileTrigger {

	public enum State {

		Empty, Contains, Space, Full
	};
	public State state;

	public TriggerFluidContainer(State state) {
		super("buildcraft:fluid." + state.name().toLowerCase(Locale.ENGLISH), "buildcraft.fluid." + state.name().toLowerCase(Locale.ENGLISH));
		this.state = state;
	}

	@Override
	public boolean hasParameter() {
		return state == State.Contains || state == State.Space;
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.trigger.fluid." + state.name().toLowerCase(Locale.ENGLISH));
	}

	@Override
	public boolean isTriggerActive(ForgeDirection side, TileEntity tile, ITriggerParameter parameter) {
		if (tile instanceof IFluidHandler) {
			IFluidHandler container = (IFluidHandler) tile;

			FluidStack searchedFluid = null;

			if (parameter != null && parameter.getItemStack() != null) {
				searchedFluid = FluidContainerRegistry.getFluidForFilledItem(parameter.getItemStack());
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
						if (c.fluid != null && c.fluid.amount > 0 && (searchedFluid == null || searchedFluid.isFluidEqual(c.fluid))) {
							return false;
						}
					}
					return true;
				case Contains:
					for (FluidTankInfo c : liquids) {
						if (c.fluid != null && c.fluid.amount > 0 && (searchedFluid == null || searchedFluid.isFluidEqual(c.fluid))) {
							return true;
						}
					}
					return false;
				case Space:
					if (searchedFluid == null) {
						for (FluidTankInfo c : liquids) {
							if (c.fluid == null || c.fluid.amount < c.capacity) {
								return true;
							}
						}
						return false;
					}
					return container.fill(side, searchedFluid, false) > 0;
				case Full:
					if (searchedFluid == null) {
						for (FluidTankInfo c : liquids) {
							if (c.fluid == null || c.fluid.amount < c.capacity) {
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
	public int getIconIndex() {
		switch (state) {
			case Empty:
				return ActionTriggerIconProvider.Trigger_FluidContainer_Empty;
			case Contains:
				return ActionTriggerIconProvider.Trigger_FluidContainer_Contains;
			case Space:
				return ActionTriggerIconProvider.Trigger_FluidContainer_Space;
			default:
				return ActionTriggerIconProvider.Trigger_FluidContainer_Full;
		}
	}

	@Override
	public ITrigger rotateLeft() {
		return this;
	}
}
