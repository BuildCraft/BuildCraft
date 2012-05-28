package net.minecraft.src.buildcraft.transport.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.NetServerHandler;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.core.network.PacketCoordinates;
import net.minecraft.src.buildcraft.core.network.PacketIds;
import net.minecraft.src.buildcraft.core.network.PacketSlotChange;
import net.minecraft.src.buildcraft.core.network.PacketUpdate;
import net.minecraft.src.buildcraft.factory.TileAssemblyTable;
import net.minecraft.src.buildcraft.transport.PipeLogicDiamond;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;
import net.minecraft.src.buildcraft.transport.CraftingGateInterface;
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

			case PacketIds.DIAMOND_PIPE_SELECT:
				PacketSlotChange packet = new PacketSlotChange();
				packet.readData(data);
				onDiamondPipeSelect(net.getPlayerEntity(), packet);
				break;
				
			case PacketIds.GATE_REQUEST_INIT:
				PacketCoordinates packetU = new PacketCoordinates();
				packetU.readData(data);
				onGateInitRequest(net.getPlayerEntity(), packetU);
				break;
				
			case PacketIds.GATE_REQUEST_SELECTION:
				PacketCoordinates packetS = new PacketCoordinates();
				packetS.readData(data);
				onGateSelectionRequest(net.getPlayerEntity(), packetS);
				break;
				
			case PacketIds.GATE_SELECTION_CHANGE:
				PacketUpdate packetC = new PacketUpdate();
				packetC.readData(data);
				onGateSelectionChange(net.getPlayerEntity(), packetC);
				break;
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Handles selection changes on a gate.
	 * @param playerEntity
	 * @param packet
	 */
	private void onGateSelectionChange(EntityPlayerMP playerEntity, PacketUpdate packet) {
		if(!(playerEntity.craftingInventory instanceof CraftingGateInterface))
			return;
	
		((CraftingGateInterface)playerEntity.craftingInventory).handleSelectionChange(packet);
	}

	/**
	 * Handles gate gui (current) selection requests.
	 * @param playerEntity
	 * @param packet
	 */
	private void onGateSelectionRequest(EntityPlayerMP playerEntity, PacketCoordinates packet) {
		if(!(playerEntity.craftingInventory instanceof CraftingGateInterface))
			return;
	
		((CraftingGateInterface)playerEntity.craftingInventory).sendSelection(playerEntity);
	}

	/**
	 * Handles received gate gui initialization requests.
	 * @param playerEntity
	 * @param packet
	 */
	private void onGateInitRequest(EntityPlayerMP playerEntity, PacketCoordinates packet) {
		if(!(playerEntity.craftingInventory instanceof CraftingGateInterface))
				return;
		
		((CraftingGateInterface)playerEntity.craftingInventory).handleInitRequest(playerEntity);
	}

	/**
	 * Retrieves pipe at specified coordinates if any.
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private TileGenericPipe getPipe(World world, int x, int y, int z) {
		if(!world.blockExists(x, y, z))
			return null;
		
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if(!(tile instanceof TileGenericPipe))
			return null;

		return (TileGenericPipe)tile;
	}
	
	/**
	 * Handles selection changes on diamond pipe guis.
	 * @param player
	 * @param packet
	 */
	private void onDiamondPipeSelect(EntityPlayerMP player, PacketSlotChange packet) {
		TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if(pipe == null)
			return;
		
		if(!(pipe.pipe.logic instanceof PipeLogicDiamond))
			return;
		
		pipe.pipe.logic.setInventorySlotContents(packet.slot, packet.stack);
	}

}
