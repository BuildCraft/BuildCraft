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
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.Trigger;
import buildcraft.core.DefaultProps;

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

			LiquidStack searchedLiquid = null;

			if (parameter != null && parameter.getItem() != null) {
				searchedLiquid = LiquidContainerRegistry.getLiquidForFilledItem(parameter.getItem());
			}

			if (searchedLiquid != null) {
				searchedLiquid.amount = 1;
			}

			ILiquidTank[] liquids = container.getTanks(ForgeDirection.UNKNOWN);

			if (liquids == null || liquids.length == 0)
				return false;

			switch (state) {
			case Empty:
				for (ILiquidTank c : liquids) {
					if (searchedLiquid != null) {
						LiquidStack drained = c.drain(1, false);
						if (drained != null && searchedLiquid.isLiquidEqual(drained))
							return false;
					} else if (c.getLiquid() != null && c.getLiquid().amount > 0)
						return false;
				}

				return true;
			case Contains:
				for (ILiquidTank c : liquids) {
					if (c.getLiquid() != null && c.getLiquid().amount != 0) {
						if (searchedLiquid == null || searchedLiquid.isLiquidEqual(c.getLiquid()))
							return true;
					}
				}

				return false;

			case Space:
				for (ILiquidTank c : liquids) {
					if (searchedLiquid != null) {
						if (c.fill(searchedLiquid, false) > 0)
							return true;
					} else if (c.getLiquid() == null || c.getLiquid().amount < c.getCapacity())
						return true;
				}

				return false;
			case Full:
				for (ILiquidTank c : liquids) {
					if (searchedLiquid != null) {
						if (c.fill(searchedLiquid, false) > 0)
							return false;
					} else if (c.getLiquid() == null || c.getLiquid().amount < c.getCapacity())
						return false;
				}

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
