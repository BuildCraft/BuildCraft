/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport.triggers;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.triggers.ActionTriggerIconProvider;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.transport.EntityData;
import buildcraft.transport.ITriggerPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.PipeTransportLiquids;
import buildcraft.transport.PipeTransportPower;

public class TriggerPipeContents extends BCTrigger implements ITriggerPipe {

	public enum Kind {
		Empty, ContainsItems, ContainsLiquids, ContainsEnergy
	};

	Kind kind;

	public TriggerPipeContents(int id, Kind kind) {
		super(id);
		this.kind = kind;
	}

	@Override
	public boolean hasParameter() {
		switch (kind) {
		case ContainsItems:
		case ContainsLiquids:
			return true;
		default:
			return false;
		}
	}

	@Override
	public String getDescription() {

		switch (kind) {
		case Empty:
			return "Pipe Empty";
		case ContainsItems:
			return "Items Traversing";
		case ContainsLiquids:
			return "Liquid Traversing";
		case ContainsEnergy:
			return "Power Traversing";
		}

		return "";
	}

	@Override
	public boolean isTriggerActive(Pipe pipe, ITriggerParameter parameter) {
		if (pipe.transport instanceof PipeTransportItems) {
			PipeTransportItems transportItems = (PipeTransportItems) pipe.transport;

			if (kind == Kind.Empty)
				return transportItems.travelingEntities.isEmpty();
			else if (kind == Kind.ContainsItems)
				if (parameter != null && parameter.getItem() != null) {
					for (EntityData data : transportItems.travelingEntities.values())
						if (data.item.getItemStack().itemID == parameter.getItem().itemID
								&& data.item.getItemStack().getItemDamage() == parameter.getItem().getItemDamage())
							return true;
				} else
					return !transportItems.travelingEntities.isEmpty();
		} else if (pipe.transport instanceof PipeTransportLiquids) {
			PipeTransportLiquids transportLiquids = (PipeTransportLiquids) pipe.transport;

			LiquidStack searchedLiquid = null;

			if (parameter != null && parameter.getItem() != null) {
				searchedLiquid = LiquidContainerRegistry.getLiquidForFilledItem(parameter.getItem());
			}

			if (kind == Kind.Empty) {
				for (ILiquidTank b : transportLiquids.getTanks(ForgeDirection.UNKNOWN))
					if (b.getLiquid() != null && b.getLiquid().amount != 0)
						return false;

				return true;
			} else {
				for (ILiquidTank b : transportLiquids.getTanks(ForgeDirection.UNKNOWN))
					if (b.getLiquid() != null && b.getLiquid().amount != 0)
						if (searchedLiquid == null || searchedLiquid.isLiquidEqual(b.getLiquid()))
							return true;

				return false;
			}
		} else if (pipe.transport instanceof PipeTransportPower) {
			PipeTransportPower transportPower = (PipeTransportPower) pipe.transport;

			if (kind == Kind.Empty) {
				for (short s : transportPower.displayPower)
					if (s != 0)
						return false;

				return true;
			} else {
				for (short s : transportPower.displayPower)
					if (s != 0)
						return true;

				return false;
			}
		}

		return false;
	}

	@Override
	public int getIconIndex() {
		switch (kind) {
			case Empty:
				return ActionTriggerIconProvider.Trigger_PipeContents_Empty;
			case ContainsItems:
				return ActionTriggerIconProvider.Trigger_PipeContents_ContainsItems;
			case ContainsLiquids:
				return ActionTriggerIconProvider.Trigger_PipeContents_ContainsLiquid;
			case ContainsEnergy:
			default:
				return ActionTriggerIconProvider.Trigger_PipeContents_ContainsEnergy;
			
		}
	}
}
