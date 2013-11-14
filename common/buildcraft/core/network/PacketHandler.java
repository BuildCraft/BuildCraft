package buildcraft.core.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {

	private void onTileUpdate(EntityPlayer player, PacketTileUpdate packet) throws IOException {
		World world = player.worldObj;

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
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		try {

			int packetID = data.read();
			switch (packetID) {
				case PacketIds.TILE_UPDATE: {
					PacketTileUpdate packetT = new PacketTileUpdate();
					packetT.readData(data);
					onTileUpdate((EntityPlayer) player, packetT);
					break;
				}

				case PacketIds.STATE_UPDATE: {
					PacketTileState inPacket = new PacketTileState();
					inPacket.readData(data);
					World world = ((EntityPlayer) player).worldObj;
					TileEntity tile = world.getBlockTileEntity(inPacket.posX, inPacket.posY, inPacket.posZ);
					if (tile instanceof ISyncedTile) {
						inPacket.applyStates(data, (ISyncedTile) tile);
					}
					break;
				}

				case PacketIds.GUI_RETURN: {
					PacketGuiReturn packet1 = new PacketGuiReturn((EntityPlayer) player);
					packet1.readData(data);
					// onGuiReturn((EntityPlayer) player, packet1);
					break;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}
