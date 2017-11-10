/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory.gui;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;

import net.minecraftforge.fluids.Fluid;

import buildcraft.BuildCraftFactory;
import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.factory.TileRefinery;

public class ContainerRefinery extends BuildCraftContainer {
	public TileRefinery refinery;

	public ContainerRefinery(InventoryPlayer inventory, TileRefinery refinery) {
		super(0);

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
		return entityplayer.worldObj.getTileEntity(refinery.xCoord, refinery.yCoord, refinery.zCoord) == refinery;
	}

	/* SETTING AND GETTING FILTERS */
	public void setFilter(final int slot, final Fluid filter) {

		refinery.setFilter(slot, filter);

		if (refinery.getWorldObj().isRemote) {
			CommandWriter payload = new CommandWriter() {
				@Override
				public void write(ByteBuf data) {
					data.writeByte(slot);
					data.writeShort(filter != null ? filter.getID() : -1);
				}
			};
			BuildCraftFactory.instance.sendToServer(new PacketCommand(refinery, "setFilter", payload));
		}
	}

	public Fluid getFilter(int slot) {
		return refinery.getFilter(slot);
	}

	/* GUI DISPLAY UPDATES */
	@Override
	public void updateProgressBar(int i, int j) {
		refinery.getGUINetworkData(i, j);
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		for (Object crafter : crafters) {
			refinery.sendGUINetworkData(this, (ICrafting) crafter);
		}
	}
}
