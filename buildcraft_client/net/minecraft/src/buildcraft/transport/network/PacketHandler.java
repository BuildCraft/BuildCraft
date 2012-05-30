package net.minecraft.src.buildcraft.transport.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.Container;
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
import net.minecraft.src.buildcraft.transport.CraftingGateInterface;
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
			
			PacketUpdate packet = new PacketUpdate();
			switch (packetID) {
			case PacketIds.DIAMOND_PIPE_CONTENTS:
				packet = new PacketUpdate(packetID);
				packet.readData(data);
				onDiamondContents(packet);
				break;
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
			case PacketIds.GATE_ACTIONS:
				packet.readData(data);
				onGateActions(packet);
				break;
			case PacketIds.GATE_TRIGGERS:
				packet.readData(data);
				onGateTriggers(packet);
				break;
			case PacketIds.GATE_SELECTION:
				packet.readData(data);
				onGateSelection(packet);
				break;
			}

		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Handles received list of potential actions on a gate
	 * @param packet
	 */
	private void onGateActions(PacketUpdate packet) {
		Container container = ModLoader.getMinecraftInstance().thePlayer.craftingInventory;
		
		if(!(container instanceof CraftingGateInterface))
			return;
		
		((CraftingGateInterface)container).updateActions(packet);
	}

	/**
	 * Handles received list of potential triggers on a gate.
	 * @param packet
	 */
	private void onGateTriggers(PacketUpdate packet) {
		Container container = ModLoader.getMinecraftInstance().thePlayer.craftingInventory;
		
		if(!(container instanceof CraftingGateInterface))
			return;
		
		((CraftingGateInterface)container).updateTriggers(packet);
	}
	
	/**
	 * Handles received current gate selection on a gate
	 * @param packet
	 */
	private void onGateSelection(PacketUpdate packet) {
		Container container = ModLoader.getMinecraftInstance().thePlayer.craftingInventory;
		
		if (!(container instanceof CraftingGateInterface))
			return;
		
		((CraftingGateInterface)container).setSelection(packet);
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
