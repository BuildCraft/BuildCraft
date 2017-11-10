/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.client.FMLClientHandler;

import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.core.network.PacketIds;

public class PacketGuiWidget extends Packet {

	private byte windowId, widgetId;
	private byte[] payload;

	public PacketGuiWidget() {
		super();
	}

	public PacketGuiWidget(int windowId, int widgetId, byte[] data) {
		this.windowId = (byte) windowId;
		this.widgetId = (byte) widgetId;
		this.payload = data;
	}

	@Override
	public void writeData(ByteBuf data) {
		data.writeByte(windowId);
		data.writeByte(widgetId);
		data.writeBytes(payload);
	}

	@Override
	public void readData(ByteBuf data) {
		windowId = data.readByte();
		widgetId = data.readByte();

		EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;

		if (player.openContainer instanceof BuildCraftContainer && player.openContainer.windowId == windowId) {
			((BuildCraftContainer) player.openContainer).handleWidgetClientData(widgetId, data);
		}
	}

	@Override
	public int getID() {
		return PacketIds.GUI_WIDGET;
	}

}
