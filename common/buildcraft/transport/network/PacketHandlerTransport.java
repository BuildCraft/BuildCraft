package buildcraft.transport.network;

import buildcraft.core.network.PacketCoordinates;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketSlotChange;
import buildcraft.core.network.PacketUpdate;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.gui.ContainerGateInterface;
import buildcraft.transport.pipes.PipeItemsDiamond;
import buildcraft.transport.pipes.PipeItemsEmerald;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PacketHandlerTransport implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet2, Player player) {
		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet2.data));
		try {
			// NetClientHandler net = (NetClientHandler) network.getNetHandler();

			int packetID = data.read();

			PacketUpdate packet = new PacketUpdate();
			switch (packetID) {
			case PacketIds.PIPE_POWER:
				PacketPowerUpdate packetPower = new PacketPowerUpdate();
				packetPower.readData(data);
				onPacketPower((EntityPlayer) player, packetPower);
				break;
			case PacketIds.PIPE_LIQUID:
				PacketFluidUpdate packetFluid = new PacketFluidUpdate();
				packetFluid.readData(data);
				break;
			case PacketIds.PIPE_CONTENTS:
				PacketPipeTransportContent packetC = new PacketPipeTransportContent();
				packetC.readData(data);
				onPipeContentUpdate((EntityPlayer) player, packetC);
				break;
			case PacketIds.GATE_ACTIONS:
				packet.readData(data);
				onGateActions((EntityPlayer) player, packet);
				break;
			case PacketIds.GATE_TRIGGERS:
				packet.readData(data);
				onGateTriggers((EntityPlayer) player, packet);
				break;
			case PacketIds.GATE_SELECTION:
				packet.readData(data);
				onGateSelection((EntityPlayer) player, packet);
				break;
			case PacketIds.PIPE_ITEM_NBT:
				PacketPipeTransportNBT packetD = new PacketPipeTransportNBT();
				packetD.readData(data);
				onPipeContentNBT((EntityPlayer) player, packetD);
				break;

			/** SERVER SIDE **/
			case PacketIds.DIAMOND_PIPE_SELECT: {
				PacketSlotChange packet1 = new PacketSlotChange();
				packet1.readData(data);
				onDiamondPipeSelect((EntityPlayer) player, packet1);
				break;
			}

			case PacketIds.EMERALD_PIPE_SELECT: {
				PacketSlotChange packet1 = new PacketSlotChange();
				packet1.readData(data);
				onEmeraldPipeSelect((EntityPlayer) player, packet1);
				break;
			}

			case PacketIds.GATE_REQUEST_INIT:
				PacketCoordinates packetU = new PacketCoordinates();
				packetU.readData(data);
				onGateInitRequest((EntityPlayer) player, packetU);
				break;

			case PacketIds.GATE_REQUEST_SELECTION:
				PacketCoordinates packetS = new PacketCoordinates();
				packetS.readData(data);
				onGateSelectionRequest((EntityPlayerMP) player, packetS);
				break;

			case PacketIds.GATE_SELECTION_CHANGE:
				PacketUpdate packet3 = new PacketUpdate();
				packet3.readData(data);
				onGateSelectionChange((EntityPlayerMP) player, packet3);
				break;
				
			case PacketIds.REQUEST_ITEM_NBT:
				PacketSimpleId packet4 = new PacketSimpleId();
				packet4.readData(data);
				onItemNBTRequest((EntityPlayerMP) player, packet4);
				break;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Handles received list of potential actions on a gate
	 * 
	 * @param packet
	 */
	private void onGateActions(EntityPlayer player, PacketUpdate packet) {
		Container container = player.openContainer;

		if (!(container instanceof ContainerGateInterface))
			return;

		((ContainerGateInterface) container).updateActions(packet);
	}

	/**
	 * Handles received list of potential triggers on a gate.
	 * 
	 * @param packet
	 */
	private void onGateTriggers(EntityPlayer player, PacketUpdate packet) {
		Container container = player.openContainer;

		if (!(container instanceof ContainerGateInterface))
			return;

		((ContainerGateInterface) container).updateTriggers(packet);
	}

	/**
	 * Handles received current gate selection on a gate
	 * 
	 * @param packet
	 */
	private void onGateSelection(EntityPlayer player, PacketUpdate packet) {
		Container container = player.openContainer;

		if (!(container instanceof ContainerGateInterface))
			return;

		((ContainerGateInterface) container).setSelection(packet, false);
	}

	private void onPipeContentNBT(EntityPlayer player, PacketPipeTransportNBT packet) {
		TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null)
			return;

		if (pipe.pipe == null)
			return;

		if (!(pipe.pipe.transport instanceof PipeTransportItems))
			return;

		((PipeTransportItems)pipe.pipe.transport).handleNBTPacket(packet);
	}

	/**
	 * Updates items in a pipe.
	 * 
	 * @param packet
	 */
	private void onPipeContentUpdate(EntityPlayer player, PacketPipeTransportContent packet) {
		World world = player.worldObj;

		if (!world.blockExists(packet.posX, packet.posY, packet.posZ))
			return;

		TileEntity entity = world.getBlockTileEntity(packet.posX, packet.posY, packet.posZ);
		if (!(entity instanceof TileGenericPipe))
			return;

		TileGenericPipe pipe = (TileGenericPipe) entity;
		if (pipe.pipe == null)
			return;

		if (!(pipe.pipe.transport instanceof PipeTransportItems))
			return;

		((PipeTransportItems) pipe.pipe.transport).handleItemPacket(packet);
	}

	/**
	 * Updates the display power on a power pipe
	 * 
	 * @param packetPower
	 */
	private void onPacketPower(EntityPlayer player, PacketPowerUpdate packetPower) {
		World world = player.worldObj;
		if (!world.blockExists(packetPower.posX, packetPower.posY, packetPower.posZ))
			return;

		TileEntity entity = world.getBlockTileEntity(packetPower.posX, packetPower.posY, packetPower.posZ);
		if (!(entity instanceof TileGenericPipe))
			return;

		TileGenericPipe pipe = (TileGenericPipe) entity;
		if (pipe.pipe == null)
			return;

		if (!(pipe.pipe.transport instanceof PipeTransportPower))
			return;

		((PipeTransportPower) pipe.pipe.transport).handlePowerPacket(packetPower);

	}

	/******************** SERVER ******************** **/

	/**
	 * Handles selection changes on a gate.
	 * 
	 * @param playerEntity
	 * @param packet
	 */
	private void onGateSelectionChange(EntityPlayer playerEntity, PacketUpdate packet) {
		if (!(playerEntity.openContainer instanceof ContainerGateInterface))
			return;

		((ContainerGateInterface) playerEntity.openContainer).setSelection(packet, true);
	}

	/**
	 * Handles gate gui (current) selection requests.
	 * 
	 * @param playerEntity
	 * @param packet
	 */
	private void onGateSelectionRequest(EntityPlayer playerEntity, PacketCoordinates packet) {
		if (!(playerEntity.openContainer instanceof ContainerGateInterface))
			return;

		((ContainerGateInterface) playerEntity.openContainer).sendSelection(playerEntity);
	}

	/**
	 * Handles received gate gui initialization requests.
	 * 
	 * @param playerEntity
	 * @param packet
	 */
	private void onGateInitRequest(EntityPlayer playerEntity, PacketCoordinates packet) {
		if (!(playerEntity.openContainer instanceof ContainerGateInterface))
			return;

		((ContainerGateInterface) playerEntity.openContainer).handleInitRequest(playerEntity);
	}

	/**
	 * Retrieves pipe at specified coordinates if any.
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private TileGenericPipe getPipe(World world, int x, int y, int z) {
		if (!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if (!(tile instanceof TileGenericPipe))
			return null;

		return (TileGenericPipe) tile;
	}

	/**
	 * Handles selection changes on diamond pipe guis.
	 * 
	 * @param player
	 * @param packet
	 */
	private void onDiamondPipeSelect(EntityPlayer player, PacketSlotChange packet) {
		TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null)
			return;

		if (!(pipe.pipe instanceof PipeItemsDiamond))
			return;

		((PipeItemsDiamond) pipe.pipe).getFilters().setInventorySlotContents(packet.slot, packet.stack);
	}
	
	/**
	 * Handles selection changes on emerald pipe guis.
	 * 
	 * @param player
	 * @param packet
	 */
	private void onEmeraldPipeSelect(EntityPlayer player, PacketSlotChange packet) {
		TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null)
			return;

		if (!(pipe.pipe instanceof PipeItemsEmerald))
			return;

		((PipeItemsEmerald) pipe.pipe).getFilters().setInventorySlotContents(packet.slot, packet.stack);
	}

	/**
	 * Handles the client request for tag data
	 * @param player
	 * @param packet
	 */
	private void onItemNBTRequest(EntityPlayerMP player, PacketSimpleId packet) {
		TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null)
			return;

		if (pipe.pipe == null)
			return;

		if (!(pipe.pipe.transport instanceof PipeTransportItems))
			return;

		((PipeTransportItems) pipe.pipe.transport).handleNBTRequestPacket(player, packet.entityId);
	}
}
