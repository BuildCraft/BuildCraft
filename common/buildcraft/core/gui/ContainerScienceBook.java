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

import buildcraft.core.gui.slots.SlotHidden;
import buildcraft.core.gui.slots.SlotUntouchable;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCSide;
import buildcraft.core.science.Technology;
import buildcraft.core.science.TechnologyNBT;
import buildcraft.core.utils.NBTUtils;

public class ContainerScienceBook extends BuildCraftContainer {

	public float progress = 0;

	public TechnologyNBT book;
	private EntityPlayer player;

	public ContainerScienceBook(EntityPlayer iPlayer) {
		super(iPlayer.inventory.getSizeInventory());

		player = iPlayer;

		book = TechnologyNBT.getTechnology(player, player.getHeldItem());

		for (int sy = 0; sy < 3; sy++) {
			for (int sx = 0; sx < 9; sx++) {
				addSlotToContainer(new SlotHidden(player.inventory, sx + sy * 9 + 9, 19 + sx * 18, 101 + sy * 18));
			}
		}

		for (int sx = 0; sx < 9; sx++) {
			addSlotToContainer(new SlotHidden(player.inventory, sx, 19 + sx * 18, 159));
		}

		for (int sx = 0; sx < 3; sx++) {
			addSlotToContainer(new Slot(book.inventory, sx, 198 + sx * 18, 99));
		}

		for (int sx = 0; sx < 3; sx++) {
			addSlotToContainer(new SlotUntouchable(book.leftToCompute, sx, 198 + sx * 18, 54));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

		if (player.worldObj.getTotalWorldTime() % 5 == 0) {
			RPCHandler.rpcPlayer(player, this, "updateProgress", book.getProgress());
		}
	}

	@RPC(RPCSide.CLIENT)
	private void updateProgress(float iProgress) {
		progress = iProgress;

		book.loadFromNBT(NBTUtils.getItemData(player.getHeldItem()));
	}

	public void startResearch(Technology t) {
		RPCHandler.rpcServer(this, "rpcStartResearch", t.getID());
	}

	@RPC(RPCSide.SERVER)
	private void rpcStartResearch(String id) {
		Technology t = Technology.getTechnology(id);
		book.startResearch(t);
		book.writeToNBT(NBTUtils.getItemData(player.getHeldItem()));
	}
}
