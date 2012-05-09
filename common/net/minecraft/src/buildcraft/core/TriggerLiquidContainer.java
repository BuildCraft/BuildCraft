/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.core;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.BuildCraftAPI;
import net.minecraft.src.buildcraft.api.ILiquidContainer;
import net.minecraft.src.buildcraft.api.LiquidSlot;
import net.minecraft.src.buildcraft.api.Trigger;
import net.minecraft.src.buildcraft.api.TriggerParameter;

public class TriggerLiquidContainer extends Trigger {
	public enum State {Empty, Contains, Space, Full};

	public State state;

	public TriggerLiquidContainer (int id, State state) {
		super (id);
		this.state = state;
	}

	@Override
	public int getIndexInTexture () {
		switch (state) {
		case Empty:
			return 2 * 16 + 0;
		case Contains:
			return 2 * 16 + 1;
		case Space:
			return 2 * 16 + 2;
		default:
			return 2 * 16 + 3;
		}
	}

	@Override
	public boolean hasParameter () {
		if (state == State.Contains || state == State.Space)
			return true;
		else
			return false;
	}

	@Override
	public String getDescription () {
		switch (state) {
		case Empty:
			return "Tank Empty";
		case Contains:
			return "Liquid in Tank";
		case Space:
			return "Space for Liquid";
		default:
			return "Tank Full";
		}
	}

	@Override
	public boolean isTriggerActive (TileEntity tile, TriggerParameter parameter) {
		if (tile instanceof ILiquidContainer) {
			ILiquidContainer container = (ILiquidContainer) tile;

			int seachedLiquidId = 0;

			if (parameter != null && parameter.getItem() != null)
				seachedLiquidId = BuildCraftAPI.getLiquidForFilledItem(parameter.getItem());

			LiquidSlot [] liquids = container.getLiquidSlots();

			if (liquids == null || liquids.length == 0)
				return false;

			switch (state) {
			case Empty:


				if (liquids != null && liquids.length > 0) {
					for (LiquidSlot c : liquids)
						if (c.getLiquidQty() != 0)
							return false;

					return true;
				} else
					return false;
			case Contains:
				for (LiquidSlot c : liquids)
					if (c.getLiquidQty() != 0)
						if (seachedLiquidId == 0 || seachedLiquidId == c.getLiquidId())
							return true;

				return false;

			case Space:
				for (LiquidSlot c : liquids)
					if (c.getLiquidQty() == 0)
						return true;
					else if (c.getLiquidQty() < c.getCapacity())
						if (seachedLiquidId == 0 || seachedLiquidId == c.getLiquidId())
							return true;

				return false;
			case Full:
				for (LiquidSlot c : liquids)
					if (c.getLiquidQty() < c.getCapacity())
						return false;

				return true;
			}
		}

		return false;
	}

	@Override
	public String getTextureFile() {
		return BuildCraftCore.triggerTextures;
	}
}
