package net.minecraft.src.buildcraft.silicon.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.src.GuiScreen;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NetClientHandler;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.buildcraft.core.network.PacketIds;
import net.minecraft.src.buildcraft.core.network.PacketUpdate;
import net.minecraft.src.buildcraft.factory.TileAssemblyTable;
import net.minecraft.src.buildcraft.factory.TileAssemblyTable.SelectionMessage;
import net.minecraft.src.buildcraft.silicon.GuiAssemblyTable;
import net.minecraft.src.forge.IPacketHandler;

public class PacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(NetworkManager network, String channel, byte[] bytes) {

		DataInputStream data = new DataInputStream(new ByteArrayInputStream(bytes));
		try
		{
			NetClientHandler net = (NetClientHandler)network.getNetHandler();

			int packetID = data.read();
			switch (packetID) {
			case PacketIds.SELECTION_ASSEMBLY:
				PacketUpdate packetT = new PacketUpdate();
				packetT.readData(data);
				onSelectionUpdate(packetT);
				break;
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}

	}

	private void onSelectionUpdate(PacketUpdate packet) {

		GuiScreen screen = ModLoader.getMinecraftInstance().currentScreen;

		if (screen instanceof GuiAssemblyTable) {
			GuiAssemblyTable gui = (GuiAssemblyTable) screen;
			SelectionMessage message = new SelectionMessage();

			TileAssemblyTable.selectionMessageWrapper.fromPayload(message, packet.payload);
			gui.handleSelectionMessage (message);
		}
	}

}
