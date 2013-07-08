/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core.triggers;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;
import buildcraft.api.gates.ITriggerParameter;

public class TriggerFluidContainer extends BCTrigger {

	public enum State {
		Empty, Contains, Space, Full
	};

	public State state;

	public TriggerFluidContainer(int id, State state) {
		super(id);
		this.state = state;
	}

	@Override
	public boolean hasParameter() {
		if (state == State.Contains || state == State.Space)
			return true;
		else
			return false;
	}

	@Override
	public String getDescription() {
		switch (state) {
		case Empty:
			return "Tank Empty";
		case Contains:
			return "Fluid in Tank";
		case Space:
			return "Space for Fluid";
		default:
			return "Tank Full";
		}
	}

	@Override
	public boolean isTriggerActive(ForgeDirection side, TileEntity tile, ITriggerParameter parameter) {
		if (tile instanceof IFluidHandler) {
			IFluidHandler container = (IFluidHandler) tile;

			FluidStack searchedFluid = null;

			if (parameter != null && parameter.getItem() != null) {
				searchedFluid = FluidContainerRegistry.getFluidForFilledItem(parameter.getItem());
			}

			if (searchedFluid != null) {
				searchedFluid.amount = 1;
			}

			FluidTankInfo[] liquids = container.getTankInfo(ForgeDirection.UNKNOWN);
			if (liquids == null || liquids.length == 0)
				return false;

			switch (state) {
			case Empty:
				for (FluidTankInfo c : liquids) {
					if (searchedFluid != null) {
						FluidStack drained = c.drain(1, false);
						if (drained != null && searchedFluid.isFluidEqual(drained))
							return false;
					} else if (c.getFluid() != null && c.getFluid().amount > 0)
						return false;
				}

				return true;
			case Contains:
				for (IFluidTank c : liquids) {
					if (c.getFluid() != null && c.getFluid().amount != 0) {
						if (searchedFluid == null || searchedFluid.isFluidEqual(c.getFluid()))
							return true;
					}
				}

				return false;

			case Space:
				for (IFluidTank c : liquids) {
					if (searchedFluid != null) {
						if (c.fill(searchedFluid, false) > 0)
							return true;
					} else if (c.getFluid() == null || c.getFluid().amount < c.getCapacity())
						return true;
				}

				return false;
			case Full:
				for (IFluidTank c : liquids) {
					if (searchedFluid != null) {
						if (c.fill(searchedFluid, false) > 0)
							return false;
					} else if (c.getFluid() == null || c.getFluid().amount < c.getCapacity())
						return false;
				}

				return true;
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
}
