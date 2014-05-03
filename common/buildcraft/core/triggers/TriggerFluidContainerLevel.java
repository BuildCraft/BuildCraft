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

public class TriggerFluidContainerLevel extends BCTrigger implements ITileTrigger {

	public enum TriggerType {

		BELOW_25(0.25F), BELOW_50(0.5F), BELOW_75(0.75F);

		public final float level;

		private TriggerType(float level) {
			this.level = level;
		}
	};

	public TriggerType type;

	public TriggerFluidContainerLevel(TriggerType type) {
		super("buildcraft:fluid." + type.name().toLowerCase(Locale.ENGLISH), "buildcraft.fluid." + type.name().toLowerCase(Locale.ENGLISH));
		this.type = type;
	}

	@Override
	public boolean hasParameter() {
		return true;
	}

	@Override
	public String getDescription() {
		return String.format(StringUtils.localize("gate.trigger.fluidlevel.below"), (int) (type.level * 100));
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

			for (FluidTankInfo c : liquids) {
				if (c.fluid == null) {
					if (searchedFluid == null) {
						return true;
					}
					return container.fill(side, searchedFluid, false) > 0;
				}

				if (searchedFluid == null || searchedFluid.isFluidEqual(c.fluid)) {
					float percentage = (float) c.fluid.amount / (float) c.capacity;
					return percentage < type.level;
				}
			}
		}

		return false;
	}

	@Override
	public int getIconIndex() {
		switch (type) {
			case BELOW_25:
				return ActionTriggerIconProvider.Trigger_FluidContainer_Below25;
			case BELOW_50:
				return ActionTriggerIconProvider.Trigger_FluidContainer_Below50;
			case BELOW_75:
			default:
				return ActionTriggerIconProvider.Trigger_FluidContainer_Below75;
		}
	}

	@Override
	public ITrigger rotateLeft() {
		return this;
	}
}
