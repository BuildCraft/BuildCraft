/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.statements;

import java.util.Locale;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.IInvSlot;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.core.lib.inventory.InventoryIterator;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.utils.StringUtils;

public class TriggerInventoryLevel extends BCStatement implements ITriggerExternal {

	public enum TriggerType {

		BELOW25(0.25F), BELOW50(0.5F), BELOW75(0.75F);
		public final float level;

		TriggerType(float level) {
			this.level = level;
		}
	}

	public TriggerType type;

	public TriggerInventoryLevel(TriggerType type) {
		super("buildcraft:inventorylevel." + type.name().toLowerCase(Locale.ENGLISH),
				"buildcraft.inventorylevel." + type.name().toLowerCase(Locale.ENGLISH),
				"buildcraft.filteredBuffer." + type.name().toLowerCase(Locale.ENGLISH));
		this.type = type;
	}

	@Override
	public int maxParameters() {
		return 1;
	}

	@Override
	public int minParameters() {
		return 1;
	}

	@Override
	public String getDescription() {
		return String.format(StringUtils.localize("gate.trigger.inventorylevel.below"), (int) (type.level * 100));
	}

	@Override
	public boolean isTriggerActive(TileEntity tile, ForgeDirection side, IStatementContainer container, IStatementParameter[] parameters) {
		// A parameter is required
		if (parameters == null || parameters.length < 1 || parameters[0] == null) {
			return false;
		}

		if (tile instanceof IInventory) {
			IInventory inventory = (IInventory) tile;
			ItemStack searchStack = parameters[0].getItemStack();

			if (searchStack == null) {
				return false;
			}

			int stackSpace = 0;
			int foundItems = 0;
			for (IInvSlot slot : InventoryIterator.getIterable(inventory, side.getOpposite())) {
				if (slot.canPutStackInSlot(searchStack)) {
					ItemStack stackInSlot = slot.getStackInSlot();
					if (stackInSlot == null || StackHelper.canStacksOrListsMerge(stackInSlot, searchStack)) {
						stackSpace++;
						foundItems += stackInSlot == null ? 0 : stackInSlot.stackSize;
					}
				}
			}

			if (stackSpace > 0) {
				float percentage = foundItems / ((float) stackSpace * (float) Math.min(searchStack.getMaxStackSize(), inventory.getInventoryStackLimit()));
				return percentage < type.level;
			}

		}

		return false;
	}

	@Override
	public void registerIcons(IIconRegister register) {
		icon = register.registerIcon("buildcraftcore:triggers/trigger_inventory_" + type.name().toLowerCase());
	}

	@Override
	public IStatementParameter createParameter(int index) {
		return new StatementParameterItemStack();
	}
}
