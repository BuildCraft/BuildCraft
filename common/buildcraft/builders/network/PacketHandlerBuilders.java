package buildcraft.builders.network;

import buildcraft.builders.TileArchitect;
import buildcraft.builders.TileBlueprintLibrary;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketUpdate;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;

public class PacketHandlerBuilders implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {

		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		try {
			int packetID = data.read();
			switch (packetID) {
			// FIXME: Replace that by a RPC
			case PacketIds.ARCHITECT_NAME:
				PacketUpdate packetA = new PacketUpdate();
				packetA.readData(data);
				onArchitectName((EntityPlayer) player, packetA);
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void onArchitectName(EntityPlayer player, PacketUpdate packet) {
		// FIXME: Not much point to port this to stream payload at this stage,
		// should use a RPC instead.
		/*TileEntity te = player.worldObj.getBlockTileEntity(packet.posX, packet.posY, packet.posZ);
		if (te instanceof TileArchitect) {
			((TileArchitect) te).handleClientInput((char) ((PacketPayloadArrays)packet.payload).intPayload[0]);
		}*/
	}

}
