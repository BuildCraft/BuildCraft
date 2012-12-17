/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.factory.TileRefinery;

public class ContainerRefinery extends BuildCraftContainer {

	TileRefinery refinery;

	public ContainerRefinery(InventoryPlayer inventory, TileRefinery refinery) {
		super(refinery.getSizeInventory());

		for (int l = 0; l < 3; l++) {
			for (int k1 = 0; k1 < 9; k1++) {
				addSlotToContainer(new Slot(inventory, k1 + l * 9 + 9, 8 + k1 * 18, 123 + l * 18));
			}
		}

		for (int i1 = 0; i1 < 9; i1++) {
			addSlotToContainer(new Slot(inventory, i1, 8 + i1 * 18, 181));
		}

		this.refinery = refinery;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return refinery.isUseableByPlayer(entityplayer);
	}

	/* SETTING AND GETTING FILTERS */
	/**
	 * @param slot
	 *            @ param liquidId param liquidMeta (for future use)
	 */
	public void setFilter(int slot, int liquidId, int liquidMeta) {

		refinery.setFilter(slot, liquidId, liquidMeta);

		if (CoreProxy.proxy.isRenderWorld(refinery.worldObj)) {
			PacketPayload payload = new PacketPayload(3, 0, 0);
			payload.intPayload[0] = slot;
			payload.intPayload[1] = liquidId;
			payload.intPayload[2] = liquidMeta;
			CoreProxy.proxy.sendToServer(new PacketUpdate(PacketIds.REFINERY_FILTER_SET, refinery.xCoord, refinery.yCoord, refinery.zCoord, payload)
					.getPacket());
		}
	}

	public ItemStack getFilter(int slot) {
		int liquidId = refinery.getFilter(slot);
		int liquidMeta = refinery.getFilterMeta(slot);
		if (liquidId > 0)
			return new ItemStack(liquidId, 0, liquidMeta);
		else
			return null;
	}

	/* GUI DISPLAY UPDATES */
	// @Override Client side only
	public void updateProgressBar(int i, int j) {
		refinery.getGUINetworkData(i, j);
	}

	@Override
	public void updateCraftingResults() {
		super.updateCraftingResults();
		for (int i = 0; i < crafters.size(); i++) {
			refinery.sendGUINetworkData(this, (ICrafting) crafters.get(i));
		}
	}
}
