package net.minecraft.src.buildcraft.core.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.src.ModLoader;
import net.minecraft.src.NetClientHandler;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
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
			case PacketIds.TILE_UPDATE:
				PacketTileUpdate packetT = new PacketTileUpdate();
				packetT.readData(data);
				onTileUpdate(packetT);
				break;
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}

	}

	private void onTileUpdate(PacketTileUpdate packet) {
		
		World world = ModLoader.getMinecraftInstance().theWorld;

		if(!packet.targetExists(world))
			return;

		TileEntity entity = packet.getTarget(world);
		if(!(entity instanceof ISynchronizedTile))
			return;

		ISynchronizedTile tile = (ISynchronizedTile)entity;
		tile.handleUpdatePacket(packet);
		tile.postPacketHandling(packet);
	}
}
