/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.gui;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.relauncher.Side;

import buildcraft.BuildCraftCore;
import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.ICommandReceiver;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.robotics.TileRequester;

public class ContainerRequester extends BuildCraftContainer implements ICommandReceiver {

	public GuiRequester gui;

	public ItemStack[] requests = new ItemStack[TileRequester.NB_ITEMS];

	private TileRequester requester;

	public ContainerRequester(IInventory playerInventory, TileRequester iRequester) {
		super(iRequester.getSizeInventory());

		requester = iRequester;

		for (int x = 0; x < 4; ++x) {
			for (int y = 0; y < 5; ++y) {
				addSlotToContainer(new Slot(iRequester, x * 5 + y, 117 + x * 18, 7 + y * 18));
			}
		}

		// Player inventory
		for (int l = 0; l < 3; l++) {
			for (int k1 = 0; k1 < 9; k1++) {
				addSlotToContainer(new Slot(playerInventory, k1 + l * 9 + 9, 19 + k1 * 18, 101 + l * 18));
			}
		}

		for (int i1 = 0; i1 < 9; i1++) {
			addSlotToContainer(new Slot(playerInventory, i1, 19 + i1 * 18, 159));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

	public void getRequestList() {
		BuildCraftCore.instance.sendToServer(new PacketCommand(this, "getRequestList", null));
	}

	@Override
	public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
		if (side.isServer() && "getRequestList".equals(command)) {
			final ItemStack[] stacks = new ItemStack[TileRequester.NB_ITEMS];

			for (int i = 0; i < TileRequester.NB_ITEMS; ++i) {
				stacks[i] = requester.getRequestTemplate(i);
			}

			BuildCraftCore.instance.sendToPlayer((EntityPlayer) sender, new PacketCommand(this, "receiveRequestList",
					new CommandWriter() {
						public void write(ByteBuf data) {
							for (ItemStack s : stacks) {
								NetworkUtils.writeStack(data, s);
							}
						}
					}));
		} else if (side.isClient() && "receiveRequestList".equals(command)) {
			requests = new ItemStack[TileRequester.NB_ITEMS];
			for (int i = 0; i < TileRequester.NB_ITEMS; i++) {
				requests[i] = NetworkUtils.readStack(stream);
			}
		}
	}
}
