package buildcraft.core.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

import buildcraft.core.network.ISynchronizedTile;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketTileUpdate;

import net.minecraft.src.ModLoader;
import net.minecraft.src.NetClientHandler;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class PacketHandler implements IPacketHandler {

	private void onTileUpdate(PacketTileUpdate packet) {
		World world = ModLoader.getMinecraftInstance().theWorld;

		if (!packet.targetExists(world))
			return;

		TileEntity entity = packet.getTarget(world);
		if (!(entity instanceof ISynchronizedTile))
			return;

		ISynchronizedTile tile = (ISynchronizedTile) entity;
		tile.handleUpdatePacket(packet);
		tile.postPacketHandling(packet);
	}

	@Override
	public void onPacketData(NetworkManager manager, Packet250CustomPayload packet, Player player) {
		 DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		try {
		
			int packetID = data.read();
			switch (packetID) {
			case PacketIds.TILE_UPDATE:
				PacketTileUpdate packetT = new PacketTileUpdate();
				packetT.readData(data);
				onTileUpdate(packetT);
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
}
