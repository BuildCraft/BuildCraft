/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.buildcraft.api.BuildCraftAPI;
import net.minecraft.src.buildcraft.api.Trigger;
import net.minecraft.src.buildcraft.api.TriggerParameter;
import net.minecraft.src.buildcraft.transport.PipeTransportLiquids.LiquidBuffer;

public class TriggerPipeContents extends Trigger implements ITriggerPipe {

	public enum Kind {Empty, ContainsItems, ContainsLiquids, ContainsEnergy};

	Kind kind;

	public TriggerPipeContents (int id, Kind kind) {
		super (id);
		this.kind = kind;
	}

	@Override
	public int getIndexInTexture () {
		switch (kind) {
		case Empty:
			return 3 * 16 + 0;
		case ContainsItems:
			return 3 * 16 + 1;
		case ContainsLiquids:
			return 3 * 16 + 2;
		case ContainsEnergy:
			return 3 * 16 + 3;
		}
		return 3 * 16 + 0;
	}

	@Override
	public boolean hasParameter () {
		switch (kind) {
		case ContainsItems:
		case ContainsLiquids:
			return true;
		}

		return false;
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
	public boolean isTriggerActive(Pipe pipe, TriggerParameter parameter) {
		if (pipe.transport instanceof PipeTransportItems) {
			PipeTransportItems transportItems = (PipeTransportItems) pipe.transport;

			if (kind == Kind.Empty)
				return transportItems.travelingEntities.isEmpty();
			else if (kind == Kind.ContainsItems)
				if (parameter != null && parameter.getItem() != null) {
					for (EntityData data : transportItems.travelingEntities
							.values())
						if (data.item.item.itemID == parameter.getItem()
								.itemID
								&& data.item.item.getItemDamage() == parameter
										.getItem().getItemDamage())
							return true;
				} else
					return !transportItems.travelingEntities.isEmpty();
		} else if (pipe.transport instanceof PipeTransportLiquids) {
			PipeTransportLiquids transportLiquids = (PipeTransportLiquids) pipe.transport;

			int seachedLiquidId = 0;

			if (parameter != null && parameter.getItem() != null)
				seachedLiquidId = BuildCraftAPI.getLiquidForFilledItem(parameter.getItem());

			if (kind == Kind.Empty) {
				for (LiquidBuffer b : transportLiquids.side)
					if (b.average != 0)
						return false;

				return true;
			} else {
				for (LiquidBuffer b : transportLiquids.side)
					if (b.average != 0)
						if (seachedLiquidId == 0 || b.liquidId == seachedLiquidId)
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
	public String getTextureFile() {
		return BuildCraftCore.triggerTextures;
	}

}
