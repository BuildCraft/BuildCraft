/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.triggers;

import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.triggers.ActionTriggerIconProvider;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.transport.TileFilteredBuffer;
import java.util.Locale;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class TriggerFilteredBufferInventoryLevel extends BCTrigger {

	public enum State {

		Below25, Below50, Below75
	};
	public State state;

	public TriggerFilteredBufferInventoryLevel(int legacyId, State state) {
		super(legacyId, "buildcraft.filteredBuffer." + state.name().toLowerCase(Locale.ENGLISH));

		this.state = state;
	}

	@Override
	public boolean hasParameter() {
		return true;
	}

	@Override
	public String getDescription() {
		switch (state) {
			case Below25:
				return "Contains < 25%";
			case Below50:
				return "Contains < 50%";
			default:
				return "Contains < 75%";
		}
	}

	@Override
	public boolean isTriggerActive(ForgeDirection side, TileEntity tile, ITriggerParameter parameter) {
		if (tile instanceof TileFilteredBuffer) {

			// A parameter is required
			if (parameter == null) {
				return false;
			}

			TileFilteredBuffer filteredBuffer = (TileFilteredBuffer) tile;
			ItemStack searchStack = parameter.getItem();

			int foundStackCount = 0;
			int foundItemCount = 0;

			IInventory filters = filteredBuffer.getFilters();

			for (int i = 0; i < filters.getSizeInventory(); i++) {
				ItemStack filterStack = filters.getStackInSlot(i);

				if (filterStack != null && filterStack.isItemEqual(searchStack)) {
					ItemStack foundStack = filteredBuffer.getStackInSlot(i);

					if (foundStack != null)
						foundItemCount += foundStack.stackSize;

					foundStackCount++;
				}
			}

			if (foundStackCount > 0) {
				float percentage = (float) foundItemCount / ((float) foundStackCount * (float) searchStack.getMaxStackSize());

				switch (state) {
					case Below25:
						return percentage < 0.25f;
					case Below50:
						return percentage < 0.5f;
					default:
						return percentage < 0.75f;
				}
			}
		}

		return false;
	}

	@Override
	public int getIconIndex() {
		switch (state) {
			case Below25:
				return ActionTriggerIconProvider.Trigger_Inventory_Below25;
			case Below50:
				return ActionTriggerIconProvider.Trigger_Inventory_Below50;
			default:
				return ActionTriggerIconProvider.Trigger_Inventory_Below75;
		}
	}
}
