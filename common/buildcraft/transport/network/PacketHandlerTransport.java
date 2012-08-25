package buildcraft.transport.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import buildcraft.core.network.PacketCoordinates;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketNBT;
import buildcraft.core.network.PacketPipeTransportContent;
import buildcraft.core.network.PacketSlotChange;
import buildcraft.core.network.PacketUpdate;
import buildcraft.transport.PipeLogicDiamond;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.PipeTransportLiquids;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.gui.ContainerGateInterface;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandlerTransport implements IPacketHandler {

	@Override
	public void onPacketData(NetworkManager manager, Packet250CustomPayload packet2, Player player) {
		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet2.data));
		try {
			//NetClientHandler net = (NetClientHandler) network.getNetHandler();

			int packetID = data.read();

			PacketUpdate packet = new PacketUpdate();
			switch (packetID) {
			case PacketIds.DIAMOND_PIPE_CONTENTS:
				PacketNBT packetN = new PacketNBT();
				packetN.readData(data);
				onDiamondContents(packetN);
				break;
			case PacketIds.PIPE_POWER:
				PacketPowerUpdate packetPower= new PacketPowerUpdate();
				packetPower.readData(data);
				onPacketPower(packetPower);
				break;
			case PacketIds.PIPE_LIQUID:
				PacketLiquidUpdate packetLiquid = new PacketLiquidUpdate();
				packetLiquid.readData(data);
				onPacketLiquid(packetLiquid);
				break;
			case PacketIds.PIPE_DESCRIPTION:
				PipeRenderStatePacket descPacket = new PipeRenderStatePacket();
				descPacket.readData(data);
				onPipeDescription(descPacket);
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

			/** SERVER SIDE **/
			case PacketIds.DIAMOND_PIPE_SELECT:
				PacketSlotChange packet1 = new PacketSlotChange();
				packet1.readData(data);
				onDiamondPipeSelect((EntityPlayerMP) player, packet1);
				break;

			case PacketIds.GATE_REQUEST_INIT:
				PacketCoordinates packetU = new PacketCoordinates();
				packetU.readData(data);
				onGateInitRequest((EntityPlayerMP) player, packetU);
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
	private void onGateActions(PacketUpdate packet) {
		Container container = FMLClientHandler.instance().getClient().thePlayer.craftingInventory;

		if (!(container instanceof ContainerGateInterface))
			return;

		((ContainerGateInterface) container).updateActions(packet);
	}

	/**
	 * Handles received list of potential triggers on a gate.
	 *
	 * @param packet
	 */
	private void onGateTriggers(PacketUpdate packet) {
		Container container = FMLClientHandler.instance().getClient().thePlayer.craftingInventory;

		if (!(container instanceof ContainerGateInterface))
			return;

		((ContainerGateInterface) container).updateTriggers(packet);
	}

	/**
	 * Handles received current gate selection on a gate
	 *
	 * @param packet
	 */
	private void onGateSelection(PacketUpdate packet) {
		Container container = FMLClientHandler.instance().getClient().thePlayer.craftingInventory;

		if (!(container instanceof ContainerGateInterface))
			return;

		((ContainerGateInterface) container).setSelection(packet);
	}

	/**
	 * Handles a pipe description packet. (Creates the pipe object client side
	 * if needed.)
	 *
	 * @param descPacket
	 */
	private void onPipeDescription(PipeRenderStatePacket descPacket) {
		World world = FMLClientHandler.instance().getClient().theWorld;

		if (!world.blockExists(descPacket.posX, descPacket.posY, descPacket.posZ))
			return;

		TileEntity entity = world.getBlockTileEntity(descPacket.posX, descPacket.posY, descPacket.posZ);
		if (entity == null){
			return;
//			entity = new TileGenericPipeProxy();
//			world.setBlockTileEntity(descPacket.posX, descPacket.posY, descPacket.posZ, entity);
		}

		if (!(entity instanceof TileGenericPipe))
			return;

		TileGenericPipe tile = (TileGenericPipe) entity;
		tile.handleDescriptionPacket(descPacket);
	}

	/**
	 * Updates items in a pipe.
	 *
	 * @param packet
	 */
	private void onPipeContentUpdate(PacketPipeTransportContent packet) {
		World world = FMLClientHandler.instance().getClient().theWorld;

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
	 * @param packetPower
	 */
	private void onPacketPower(PacketPowerUpdate packetPower) {
		World world = FMLClientHandler.instance().getClient().theWorld;
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

	private void onPacketLiquid(PacketLiquidUpdate packetLiquid) {
		World world = FMLClientHandler.instance().getClient().theWorld;
		if (!world.blockExists(packetLiquid.posX, packetLiquid.posY, packetLiquid.posZ))
			return;

		TileEntity entity = world.getBlockTileEntity(packetLiquid.posX, packetLiquid.posY, packetLiquid.posZ);
		if (!(entity instanceof TileGenericPipe))
			return;

		TileGenericPipe pipe = (TileGenericPipe) entity;
		if (pipe.pipe == null)
			return;

		if (!(pipe.pipe.transport instanceof PipeTransportLiquids))
			return;

		((PipeTransportLiquids) pipe.pipe.transport).handleLiquidPacket(packetLiquid);
	}

	/**
	 * Updates contents of a diamond pipe.
	 *
	 * @param packet
	 */
	private void onDiamondContents(PacketNBT packet) {

		World world = FMLClientHandler.instance().getClient().theWorld;

		if (!world.blockExists(packet.posX, packet.posY, packet.posZ))
			return;

		TileEntity entity = world.getBlockTileEntity(packet.posX, packet.posY, packet.posZ);
		if (!(entity instanceof TileGenericPipe))
			return;

		TileGenericPipe pipe = (TileGenericPipe) entity;
		if (pipe.pipe == null)
			return;

		if (!(pipe.pipe.logic instanceof PipeLogicDiamond))
			return;

		((PipeLogicDiamond) pipe.pipe.logic).handleFilterSet(packet);

		// / FIXME: Unsure how to handle this
		/*
		BlockIndex index = new BlockIndex(packet.posX, packet.posY, packet.posZ);

		if (BuildCraftCore.bufferedDescriptions.containsKey(index))
			BuildCraftCore.bufferedDescriptions.remove(index);

		BuildCraftCore.bufferedDescriptions.put(index, packet);
		*/
	}

	/********************       SERVER         ******************** **/

	/**
	 * Handles selection changes on a gate.
	 *
	 * @param playerEntity
	 * @param packet
	 */
	private void onGateSelectionChange(EntityPlayerMP playerEntity, PacketUpdate packet) {
		if (!(playerEntity.craftingInventory instanceof ContainerGateInterface))
			return;

		((ContainerGateInterface) playerEntity.craftingInventory).handleSelectionChange(packet);
	}

	/**
	 * Handles gate gui (current) selection requests.
	 *
	 * @param playerEntity
	 * @param packet
	 */
	private void onGateSelectionRequest(EntityPlayerMP playerEntity, PacketCoordinates packet) {
		if (!(playerEntity.craftingInventory instanceof ContainerGateInterface))
			return;

		((ContainerGateInterface) playerEntity.craftingInventory).sendSelection(playerEntity);
	}

	/**
	 * Handles received gate gui initialization requests.
	 *
	 * @param playerEntity
	 * @param packet
	 */
	private void onGateInitRequest(EntityPlayerMP playerEntity, PacketCoordinates packet) {
		if (!(playerEntity.craftingInventory instanceof ContainerGateInterface))
			return;

		((ContainerGateInterface) playerEntity.craftingInventory).handleInitRequest(playerEntity);
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
	private void onDiamondPipeSelect(EntityPlayerMP player, PacketSlotChange packet) {
		TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null)
			return;

		if (!(pipe.pipe.logic instanceof PipeLogicDiamond))
			return;

		((PipeLogicDiamond)pipe.pipe.logic).setInventorySlotContents(packet.slot, packet.stack);
	}


}
