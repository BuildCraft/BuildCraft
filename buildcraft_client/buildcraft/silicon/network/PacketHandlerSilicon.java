package buildcraft.silicon.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketUpdate;
import buildcraft.factory.TileAssemblyTable;
import buildcraft.factory.TileAssemblyTable.SelectionMessage;
import buildcraft.silicon.gui.GuiAssemblyTable;

import net.minecraft.src.GuiScreen;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet250CustomPayload;

public class PacketHandlerSilicon implements IPacketHandler {

	@Override
	public void onPacketData(NetworkManager manager, Packet250CustomPayload packet, Player player) {

		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		try {
			int packetID = data.read();
			switch (packetID) {
			case PacketIds.SELECTION_ASSEMBLY:
				PacketUpdate packetT = new PacketUpdate();
				packetT.readData(data);
				onSelectionUpdate(packetT);
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void onSelectionUpdate(PacketUpdate packet) {

		GuiScreen screen = ModLoader.getMinecraftInstance().currentScreen;

		if (screen instanceof GuiAssemblyTable) {
			GuiAssemblyTable gui = (GuiAssemblyTable) screen;
			SelectionMessage message = new SelectionMessage();

			TileAssemblyTable.selectionMessageWrapper.fromPayload(message, packet.payload);
			gui.handleSelectionMessage(message);
		}
	}

}
