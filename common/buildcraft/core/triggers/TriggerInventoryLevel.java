/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.triggers;

import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.inventory.InventoryIterator;
import buildcraft.core.inventory.InventoryIterator.IInvSlot;
import buildcraft.core.inventory.StackHelper;
import java.util.Locale;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class TriggerInventoryLevel extends BCTrigger {

	public enum TriggerType {

		BELOW_25, BELOW_50, BELOW_75
	};
	public TriggerType type;

	public TriggerInventoryLevel(TriggerType type) {
		super(0, "buildcraft.inventorylevel." + type.name().toLowerCase(Locale.ENGLISH));
		this.type = type;

		// Legacy migration code
		ActionManager.triggers.put("buildcraft.filteredBuffer." + type.name().toLowerCase(Locale.ENGLISH), this);
	}

	@Override
	public boolean hasParameter() {
		return true;
	}

	@Override
	public boolean requiresParameter() {
		return true;
	}

	@Override
	public String getDescription() {
		switch (type) {
			case BELOW_25:
				return "Contains < 25%";
			case BELOW_50:
				return "Contains < 50%";
			default:
				return "Contains < 75%";
		}
	}

	@Override
	public boolean isTriggerActive(ForgeDirection side, TileEntity tile, ITriggerParameter parameter) {
		// A parameter is required
		if (parameter == null)
			return false;

		if (tile instanceof IInventory) {
			ItemStack searchStack = parameter.getItemStack();

			int stackSpace = 0;
			int foundItems = 0;
			for (IInvSlot slot : InventoryIterator.getIterable((IInventory) tile, side)) {
				if (slot.canPutStackInSlot(searchStack)) {
					ItemStack stackInSlot = slot.getStackInSlot();
					if (stackInSlot == null || StackHelper.instance().canStacksMerge(stackInSlot, searchStack)) {
						stackSpace++;
						foundItems += stackInSlot == null ? 0 : stackInSlot.stackSize;
					}
				}
			}

			if (stackSpace > 0) {
				float percentage = (float) foundItems / ((float) stackSpace * (float) searchStack.getMaxStackSize());

				switch (type) {
					case BELOW_25:
						return percentage < 0.25f;
					case BELOW_50:
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
		switch (type) {
			case BELOW_25:
				return ActionTriggerIconProvider.Trigger_Inventory_Below25;
			case BELOW_50:
				return ActionTriggerIconProvider.Trigger_Inventory_Below50;
			default:
				return ActionTriggerIconProvider.Trigger_Inventory_Below75;
		}
	}
}
