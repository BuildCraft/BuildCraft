/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.statements;

import java.util.Locale;

import net.minecraft.client.renderer.texture.IIconRegister;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.core.statements.BCStatement;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.TravelingItem;

public class TriggerPipeContents extends BCStatement implements ITriggerInternal {

	public enum PipeContents {
		empty,
		containsItems,
		containsFluids,
		containsEnergy,
		requestsEnergy,
		tooMuchEnergy;
		public ITriggerInternal trigger;
	}

	private PipeContents kind;

	public TriggerPipeContents(PipeContents kind) {
		super("buildcraft:pipe.contents." + kind.name().toLowerCase(Locale.ENGLISH), "buildcraft.pipe.contents." + kind.name());
		this.kind = kind;
		kind.trigger = this;
	}

	@Override
	public int maxParameters() {
		switch (kind) {
			case containsItems:
			case containsFluids:
				return 1;
			default:
				return 0;
		}
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.trigger.pipe." + kind.name());
	}

	@Override
	public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
		if (!(container instanceof IGate)) {
			return false;
		}

		Pipe<?> pipe = (Pipe<?>) ((IGate) container).getPipe();
		IStatementParameter parameter = parameters[0];

		if (pipe.transport instanceof PipeTransportItems) {
			PipeTransportItems transportItems = (PipeTransportItems) pipe.transport;
			if (kind == PipeContents.empty) {
				return transportItems.items.isEmpty();
			} else if (kind == PipeContents.containsItems) {
				if (parameter != null && parameter.getItemStack() != null) {
					for (TravelingItem item : transportItems.items) {
						if (StackHelper.isMatchingItemOrList(parameter.getItemStack(), item.getItemStack())) {
							return true;
						}
					}
				} else {
					return !transportItems.items.isEmpty();
				}
			}
		} else if (pipe.transport instanceof PipeTransportFluids) {
			PipeTransportFluids transportFluids = (PipeTransportFluids) pipe.transport;

			if (kind == PipeContents.empty) {
				return transportFluids.fluidType == null;
			} else {
				if (parameter != null && parameter.getItemStack() != null) {
					FluidStack searchedFluid = FluidContainerRegistry.getFluidForFilledItem(parameter.getItemStack());

					if (searchedFluid != null) {
						return transportFluids.fluidType != null && searchedFluid.isFluidEqual(transportFluids.fluidType);
					}
				} else {
					return transportFluids.fluidType != null;
				}
			}
		} else if (pipe.transport instanceof PipeTransportPower) {
			PipeTransportPower transportPower = (PipeTransportPower) pipe.transport;

			switch (kind) {
				case empty:
					for (short s : transportPower.displayPower) {
						if (s > 0) {
							return false;
						}
					}

					return true;
				case containsEnergy:
					for (short s : transportPower.displayPower) {
						if (s > 0) {
							return true;
						}
					}

					return false;
				case requestsEnergy:
					return transportPower.isQueryingPower();
				default:
				case tooMuchEnergy:
					return transportPower.isOverloaded();
			}
		}

		return false;
	}

	@Override
	public IStatementParameter createParameter(int index) {
		return new StatementParameterItemStack();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcrafttransport:triggers/trigger_pipecontents_" + kind.name().toLowerCase(Locale.ENGLISH));
	}
}
