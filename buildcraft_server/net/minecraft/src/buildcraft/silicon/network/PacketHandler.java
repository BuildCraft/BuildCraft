package net.minecraft.src.buildcraft.silicon.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.NetServerHandler;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.core.network.PacketCoordinates;
import net.minecraft.src.buildcraft.core.network.PacketIds;
import net.minecraft.src.buildcraft.core.network.PacketUpdate;
import net.minecraft.src.buildcraft.factory.TileAssemblyTable;
import net.minecraft.src.forge.IPacketHandler;

public class PacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(NetworkManager network, String channel, byte[] bytes) {

		DataInputStream data = new DataInputStream(new ByteArrayInputStream(bytes));
		try
		{
			NetServerHandler net = (NetServerHandler)network.getNetHandler();

			int packetID = data.read();
			switch (packetID) {

			case PacketIds.SELECTION_ASSEMBLY:
				PacketUpdate packet = new PacketUpdate();
				packet.readData(data);
				onAssemblySelect(net.getPlayerEntity(), packet);
				break;
			case PacketIds.SELECTION_ASSEMBLY_GET:
				PacketCoordinates packetC = new PacketCoordinates();
				packetC.readData(data);
				onAssemblyGetSelection(net.getPlayerEntity(), packetC);
				break;

			}

		} catch(Exception ex) {
			ex.printStackTrace();
		}

	}

	private TileAssemblyTable getAssemblyTable(World world, int x, int y, int z) {

		if(!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if(!(tile instanceof TileAssemblyTable))
			return null;

		return (TileAssemblyTable)tile;
	}

	/**
	 * Sends the current selection on the assembly table to a player.
	 * @param player
	 * @param packet
	 */
	private void onAssemblyGetSelection(EntityPlayerMP player, PacketCoordinates packet) {

		TileAssemblyTable tile = getAssemblyTable(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(tile == null)
			return;

		tile.sendSelectionTo(player);
	}

	/**
	 * Sets the selection on an assembly table according to player request.
	 * @param player
	 * @param packet
	 */
	private void onAssemblySelect(EntityPlayerMP player, PacketUpdate packet) {

		TileAssemblyTable tile = getAssemblyTable(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(tile == null)
			return;
		
		TileAssemblyTable.SelectionMessage message = new TileAssemblyTable.SelectionMessage();
		TileAssemblyTable.selectionMessageWrapper.fromPayload(message, packet.payload);
		tile.handleSelectionMessage(message);
	}

}
