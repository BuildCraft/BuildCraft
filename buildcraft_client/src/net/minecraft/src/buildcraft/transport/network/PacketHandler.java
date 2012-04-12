package net.minecraft.src.buildcraft.transport.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NetClientHandler;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.core.BlockIndex;
import net.minecraft.src.buildcraft.core.network.ISynchronizedTile;
import net.minecraft.src.buildcraft.core.network.PacketIds;
import net.minecraft.src.buildcraft.core.network.PacketPipeTransportContent;
import net.minecraft.src.buildcraft.core.network.PacketPipeDescription;
import net.minecraft.src.buildcraft.core.network.PacketUpdate;
import net.minecraft.src.buildcraft.transport.PipeLogicDiamond;
import net.minecraft.src.buildcraft.transport.PipeTransportItems;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;
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
			case PacketIds.PIPE_DESCRIPTION:
				PacketPipeDescription packetU = new PacketPipeDescription();
				packetU.readData(data);
				onPipeDescription(packetU);
				break;
			case PacketIds.PIPE_CONTENTS:
				PacketPipeTransportContent packetC = new PacketPipeTransportContent();
				packetC.readData(data);
				onPipeContentUpdate(packetC);
				break;
			case PacketIds.DIAMOND_PIPE_CONTENTS:
				PacketUpdate packetD = new PacketUpdate(packetID);
				packetD.readData(data);
				onDiamondContents(packetD);
				break;
			}

		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Handles a pipe description packet. (Creates the pipe object client side if needed.)
	 * @param packet
	 */
	private void onPipeDescription(PacketPipeDescription packet) {
		World world = ModLoader.getMinecraftInstance().theWorld;

		if(!world.blockExists(packet.posX, packet.posY, packet.posZ))
			return;

		TileEntity entity = world.getBlockTileEntity(packet.posX, packet.posY, packet.posZ);
		if(!(entity instanceof ISynchronizedTile))
			return;

		ISynchronizedTile tile = (ISynchronizedTile)entity;
		tile.handleDescriptionPacket(packet);
	}
	
	/**
	 * Updates items in a pipe.
	 * @param packet
	 */
	private void onPipeContentUpdate(PacketPipeTransportContent packet) {
		World world = ModLoader.getMinecraftInstance().theWorld;

		if(!world.blockExists(packet.posX, packet.posY, packet.posZ))
			return;

		TileEntity entity = world.getBlockTileEntity(packet.posX, packet.posY, packet.posZ);
		if(!(entity instanceof TileGenericPipe))
			return;

		TileGenericPipe pipe = (TileGenericPipe)entity;
		if(pipe.pipe == null)
			return;

		if(!(pipe.pipe.transport instanceof PipeTransportItems))
			return;

		((PipeTransportItems)pipe.pipe.transport).handleItemPacket(packet);
	}

	/**
	 * Updates contents of a diamond pipe.
	 * @param packet
	 */
	private void onDiamondContents(PacketUpdate packet) {
		World world = ModLoader.getMinecraftInstance().theWorld;

		if(!world.blockExists(packet.posX, packet.posY, packet.posZ))
			return;

		TileEntity entity = world.getBlockTileEntity(packet.posX, packet.posY, packet.posZ);
		if(!(entity instanceof TileGenericPipe))
			return;

		TileGenericPipe pipe = (TileGenericPipe)entity;
		if(pipe.pipe == null)
			return;

		if (!(pipe.pipe.logic instanceof PipeLogicDiamond))
			return;

		((PipeLogicDiamond)pipe.pipe.logic).handleContentsPacket(packet);

		/// FIXME: Unsure how to handle this
		BlockIndex index = new BlockIndex(packet.posX, packet.posY, packet.posZ);

		if (BuildCraftCore.bufferedDescriptions.containsKey(index))
			BuildCraftCore.bufferedDescriptions.remove(index);

		BuildCraftCore.bufferedDescriptions.put(index, packet);

	}

}
