/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core;

import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.Trigger;
import buildcraft.api.liquids.ILiquidTank;
import buildcraft.api.liquids.ITankContainer;
import buildcraft.api.liquids.LiquidManager;
import net.minecraft.src.TileEntity;

public class TriggerLiquidContainer extends Trigger {

	public enum State {
		Empty, Contains, Space, Full
	};

	public State state;

	public TriggerLiquidContainer(int id, State state) {
		super(id);
		this.state = state;
	}

	@Override
	public int getIndexInTexture() {
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
			return "Liquid in Tank";
		case Space:
			return "Space for Liquid";
		default:
			return "Tank Full";
		}
	}

	@Override
	public boolean isTriggerActive(TileEntity tile, ITriggerParameter parameter) {
		if (tile instanceof ITankContainer) {
			ITankContainer container = (ITankContainer) tile;

			int seachedLiquidId = 0;

			if (parameter != null && parameter.getItem() != null)
				seachedLiquidId = LiquidManager.getLiquidIDForFilledItem(parameter.getItem());

			ILiquidTank[] liquids = container.getTanks();

			if (liquids == null || liquids.length == 0)
				return false;

			switch (state) {
			case Empty:

				if (liquids != null && liquids.length > 0) {
					for (ILiquidTank c : liquids)
						if (c.getLiquid() != null && c.getLiquid().amount != 0)
							return false;

					return true;
				} else
					return false;
			case Contains:
				for (ILiquidTank c : liquids)
					if (c.getLiquid() != null && c.getLiquid().amount != 0)
						if (seachedLiquidId == 0 || seachedLiquidId == c.getLiquid().itemID)
							return true;

				return false;

			case Space:
				for (ILiquidTank c : liquids)
					if (c.getLiquid() == null || c.getLiquid().amount == 0)
						return true;
					else if (c.getLiquid().amount < c.getCapacity())
						if (seachedLiquidId == 0 || seachedLiquidId == c.getLiquid().itemID)
							return true;

				return false;
			case Full:
				for (ILiquidTank c : liquids)
					if (c.getLiquid() == null || c.getLiquid().amount < c.getCapacity())
						return false;

				return true;
			}
		}

		return false;
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_TRIGGERS;
	}
}
