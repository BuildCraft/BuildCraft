package buildcraft.factory.network;

import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketPayloadStream;
import buildcraft.core.network.PacketUpdate;
import buildcraft.factory.TileRefinery;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;

public class PacketHandlerFactory implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {

		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		try {
			int packetID = data.read();
			PacketUpdate packetU = new PacketUpdate();

			switch (packetID) {

				case PacketIds.REFINERY_FILTER_SET:
					packetU.readData(data);
					onRefinerySelect((EntityPlayer) player, packetU);
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

	private void onRefinerySelect(EntityPlayer playerEntity, PacketUpdate packet) throws IOException {

		TileRefinery tile = getRefinery(playerEntity.worldObj, packet.posX, packet.posY, packet.posZ);
		if (tile == null || packet.payload == null)
			return;
	
		DataInputStream stream = ((PacketPayloadStream)packet.payload).stream;

		tile.setFilter(stream.readByte(), FluidRegistry.getFluid(stream.readShort()));
	}
}
