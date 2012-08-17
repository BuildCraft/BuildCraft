package buildcraft.factory.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketUpdate;
import buildcraft.factory.TileRefinery;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class PacketHandlerFactory implements IPacketHandler {

	@Override
	public void onPacketData(NetworkManager manager, Packet250CustomPayload packet, Player player) {

		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		try {
			int packetID = data.read();
			PacketUpdate packetU = new PacketUpdate();

			switch (packetID) {

			case PacketIds.REFINERY_FILTER_SET:
				packetU.readData(data);
				onRefinerySelect((EntityPlayer)player, packetU);
				break;

			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private TileRefinery getRefinery(World world, int x, int y, int z) {
		if (!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if (!(tile instanceof TileRefinery))
			return null;

		return (TileRefinery) tile;
	}

	private void onRefinerySelect(EntityPlayer playerEntity, PacketUpdate packet) {

		TileRefinery tile = getRefinery(playerEntity.worldObj, packet.posX, packet.posY, packet.posZ);
		if (tile == null)
			return;

		tile.setFilter(packet.payload.intPayload[0], packet.payload.intPayload[1]);

	}

}
