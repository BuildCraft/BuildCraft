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

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.IInvSlot;
import buildcraft.api.gates.ITileTrigger;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.inventory.InventoryIterator;
import buildcraft.core.inventory.StackHelper;
import buildcraft.core.utils.StringUtils;

public class TriggerInventoryLevel extends BCTrigger implements ITileTrigger {

	public enum TriggerType {

		BELOW_25(0.25F), BELOW_50(0.5F), BELOW_75(0.75F);
		public final float level;

		private TriggerType(float level) {
			this.level = level;
		}
	};
	public TriggerType type;

	public TriggerInventoryLevel(TriggerType type) {
		super("buildcraft:inventorylevel." + type.name().toLowerCase(Locale.ENGLISH),
				"buildcraft.inventorylevel." + type.name().toLowerCase(Locale.ENGLISH),
				"buildcraft.filteredBuffer." + type.name().toLowerCase(Locale.ENGLISH));
		this.type = type;
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
		return String.format(StringUtils.localize("gate.trigger.inventorylevel.below"), (int) (type.level * 100));
	}

	@Override
	public boolean isTriggerActive(ForgeDirection side, TileEntity tile, ITriggerParameter parameter) {
		// A parameter is required
		if (parameter == null) {
			return false;
		}

		if (tile instanceof IInventory) {
			ItemStack searchStack = parameter.getItemStack();

			int stackSpace = 0;
			int foundItems = 0;
			for (IInvSlot slot : InventoryIterator.getIterable((IInventory) tile, side)) {
				if (slot.canPutStackInSlot(searchStack)) {
					ItemStack stackInSlot = slot.getStackInSlot();
					if (stackInSlot == null || StackHelper.canStacksMerge(stackInSlot, searchStack)) {
						stackSpace++;
						foundItems += stackInSlot == null ? 0 : stackInSlot.stackSize;
					}
				}
			}

			if (stackSpace > 0) {
				float percentage = foundItems / ((float) stackSpace * (float) searchStack.getMaxStackSize());
				return percentage < type.level;
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

	@Override
	public ITrigger rotateLeft() {
		return this;
	}
}
