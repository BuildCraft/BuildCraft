/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.triggers;

import buildcraft.api.gates.ITriggerParameter;
import java.util.Locale;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TriggerFluidContainer extends BCTrigger {

	public enum State {

		Empty, Contains, Space, Full
	};
	public State state;

	public TriggerFluidContainer(int legacyId, State state) {
		super(legacyId, "buildcraft.fluid." + state.name().toLowerCase(Locale.ENGLISH));
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

			if (parameter != null && parameter.getItemStack() != null) {
				searchedFluid = FluidContainerRegistry.getFluidForFilledItem(parameter.getItemStack());
			}

			if (searchedFluid != null) {
				searchedFluid.amount = 1;
			}

			FluidTankInfo[] liquids = container.getTankInfo(side);
			if (liquids == null || liquids.length == 0)
				return false;

			switch (state) {
				case Empty:
					for (FluidTankInfo c : liquids) {
						if (c.fluid != null && c.fluid.amount > 0 && (searchedFluid == null || searchedFluid.isFluidEqual(c.fluid)))
							return false;
					}
					return true;
				case Contains:
					for (FluidTankInfo c : liquids) {
						if (c.fluid != null && c.fluid.amount > 0 && (searchedFluid == null || searchedFluid.isFluidEqual(c.fluid)))
							return true;
					}
					return false;
				case Space:
					if (searchedFluid == null) {
						for (FluidTankInfo c : liquids) {
							if (c.fluid == null || c.fluid.amount < c.capacity)
								return true;
						}
						return false;
					}
					return container.fill(side, searchedFluid, false) > 0;
				case Full:
					if (searchedFluid == null) {
						for (FluidTankInfo c : liquids) {
							if (c.fluid == null || c.fluid.amount < c.capacity)
								return false;
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
}
