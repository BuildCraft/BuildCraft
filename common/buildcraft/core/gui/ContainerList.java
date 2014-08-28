/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import buildcraft.core.ItemList;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCSide;

public class ContainerList extends BuildCraftContainer {

	private EntityPlayer player;
	public ItemList.StackLine lines[];

	public ContainerList(EntityPlayer iPlayer) {
		super(iPlayer.inventory.getSizeInventory());

		player = iPlayer;

		lines = ItemList.getLines(player.getCurrentEquippedItem());

		for (int sy = 0; sy < 3; sy++) {
			for (int sx = 0; sx < 9; sx++) {
				addSlotToContainer(new Slot(player.inventory, sx + sy * 9 + 9, 8 + sx * 18, 153 + sy * 18));
			}
		}

		for (int sx = 0; sx < 9; sx++) {
			addSlotToContainer(new Slot(player.inventory, sx, 8 + sx * 18, 211));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer p_75145_1_) {
		return true;
	}

	@RPC(RPCSide.SERVER)
	public void setStack(int lineIndex, int slotIndex, ItemStack stack) {
		lines[lineIndex].setStack(slotIndex, stack);
		ItemList.saveLine(player.getCurrentEquippedItem(), lines[lineIndex], lineIndex);

		if (player.worldObj.isRemote) {
			RPCHandler.rpcServer(this, "setStack", lineIndex, slotIndex, stack);
		}
	}

	@RPC(RPCSide.SERVER)
	public void switchButton(int lineIndex, int button) {
		if (button == 0) {
			lines[lineIndex].oreWildcard = false;
			lines[lineIndex].subitemsWildcard = !lines[lineIndex].subitemsWildcard;
		} else if (button == 1 && lines[lineIndex].isOre) {
			lines[lineIndex].subitemsWildcard = false;
			lines[lineIndex].oreWildcard = !lines[lineIndex].oreWildcard;
		}

		ItemList.saveLine(player.getCurrentEquippedItem(), lines[lineIndex], lineIndex);

		if (player.worldObj.isRemote) {
			RPCHandler.rpcServer(this, "switchButton", lineIndex, button);
		}
	}

	@RPC(RPCSide.SERVER)
	public void setLabel(String text) {
		ItemList.saveLabel(player.getCurrentEquippedItem(), text);

		if (player.worldObj.isRemote) {
			RPCHandler.rpcServer(this, "setLabel", text);
		}
	}
}
