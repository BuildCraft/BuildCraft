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
					PacketTileUpdate pkt = new PacketTileUpdate();
					pkt.readData(data);
					onTileUpdate((EntityPlayer) player, pkt);
					break;
				}

				case PacketIds.STATE_UPDATE: {
					PacketTileState pkt = new PacketTileState();
					pkt.readData(data);
					World world = ((EntityPlayer) player).worldObj;
					TileEntity tile = world.getBlockTileEntity(pkt.posX, pkt.posY, pkt.posZ);
					if (tile instanceof ISyncedTile) {
						pkt.applyStates(data, (ISyncedTile) tile);
					}
					break;
				}

				case PacketIds.GUI_RETURN: {
					PacketGuiReturn pkt = new PacketGuiReturn((EntityPlayer) player);
					pkt.readData(data);
					break;
				}

				case PacketIds.GUI_WIDGET: {
					PacketGuiWidget pkt = new PacketGuiWidget();
					pkt.readData(data);
					break;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}
