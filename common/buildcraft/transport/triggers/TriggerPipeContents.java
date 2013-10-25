/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.triggers;

import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.ITriggerPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.pipes.PipePowerWood;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

public class TriggerPipeContents extends BCTrigger implements ITriggerPipe {

	public enum Kind {

		Empty("buildcraft.pipe.contents.empty"),
		ContainsItems("buildcraft.pipe.contents.containsItems"),
		ContainsFluids("buildcraft.pipe.contents.containsFluids"),
		ContainsEnergy("buildcraft.pipe.contents.containsEnergy"),
		RequestsEnergy("buildcraft.pipe.contents.requestsEnergy"),
		TooMuchEnergy("buildcraft.pipe.contents.tooMuchEnergy");
		private Icon icon;
		public final String tag;

		private Kind(String tag) {
			this.tag = tag;
		}
	};
	Kind kind;

	public TriggerPipeContents(int id, Kind kind) {
		super(id, kind.tag);
		this.kind = kind;
	}

	@Override
	public boolean hasParameter() {
		switch (kind) {
			case ContainsItems:
			case ContainsFluids:
				return true;
			default:
				return false;
		}
	}

	@Override
	public String getDescription() {

		switch (kind) {
			case Empty:
				return StringUtils.localize("gate.pipe.empty");
			case ContainsItems:
				return StringUtils.localize("gate.pipe.containsItems");
			case ContainsFluids:
				return StringUtils.localize("gate.pipe.containsFluids");
			case ContainsEnergy:
				return StringUtils.localize("gate.pipe.containsEnergy");
			case RequestsEnergy:
				return StringUtils.localize("gate.pipe.requestsEnergy");
			case TooMuchEnergy:
				return StringUtils.localize("gate.pipe.tooMuchEnergy");
		}

		return "";
	}

	@Override
	public boolean isTriggerActive(Pipe pipe, ITriggerParameter parameter) {
		if (pipe.transport instanceof PipeTransportItems) {
			PipeTransportItems transportItems = (PipeTransportItems) pipe.transport;

			if (kind == Kind.Empty)
				return transportItems.items.isEmpty();
			else if (kind == Kind.ContainsItems)
				if (parameter != null && parameter.getItemStack()!= null) {
					for (TravelingItem item : transportItems.items) {
						if (item.getItemStack().itemID == parameter.getItemStack().itemID
								&& item.getItemStack().getItemDamage() == parameter.getItemStack().getItemDamage())
							return true;
					}
				} else
					return !transportItems.items.isEmpty();
		} else if (pipe.transport instanceof PipeTransportFluids) {
			PipeTransportFluids transportFluids = (PipeTransportFluids) pipe.transport;

			FluidStack searchedFluid = null;

			if (parameter != null && parameter.getItemStack() != null) {
				searchedFluid = FluidContainerRegistry.getFluidForFilledItem(parameter.getItemStack());
			}

			if (kind == Kind.Empty) {
				for (FluidTankInfo b : transportFluids.getTankInfo(ForgeDirection.UNKNOWN)) {
					if (b.fluid != null && b.fluid.amount != 0)
						return false;
				}

				return true;
			} else {
				for (FluidTankInfo b : transportFluids.getTankInfo(ForgeDirection.UNKNOWN)) {
					if (b.fluid != null && b.fluid.amount != 0)
						if (searchedFluid == null || searchedFluid.isFluidEqual(b.fluid))
							return true;
				}

				return false;
			}
		} else if (pipe.transport instanceof PipeTransportPower) {
			PipeTransportPower transportPower = (PipeTransportPower) pipe.transport;

			switch (kind) {
				case Empty:
					for (double s : transportPower.displayPower) {
						if (s > 1e-4)
							return false;
					}

					return true;
				case ContainsEnergy:
					for (double s : transportPower.displayPower) {
						if (s > 1e-4)
							return true;
					}

					return false;
				case RequestsEnergy:
					PipePowerWood wood = (PipePowerWood) pipe;
					return wood.requestsPower();
				default:
				case TooMuchEnergy:
					return transportPower.isOverloaded();
			}
		}

		return false;
	}

	@Override
	public Icon getIcon() {
		return kind.icon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {
		Kind.Empty.icon = iconRegister.registerIcon("buildcraft:triggers/trigger_pipecontents_empty");
		Kind.ContainsItems.icon = iconRegister.registerIcon("buildcraft:triggers/trigger_pipecontents_containsitems");
		Kind.ContainsFluids.icon = iconRegister.registerIcon("buildcraft:triggers/trigger_pipecontents_containsliquid");
		Kind.ContainsEnergy.icon = iconRegister.registerIcon("buildcraft:triggers/trigger_pipecontents_containsenergy");
		Kind.RequestsEnergy.icon = iconRegister.registerIcon("buildcraft:triggers/trigger_pipecontents_requestsenergy");
		Kind.TooMuchEnergy.icon = iconRegister.registerIcon("buildcraft:triggers/trigger_pipecontents_toomuchenergy");
	}
}
