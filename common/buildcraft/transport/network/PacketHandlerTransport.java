/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.network;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.INetHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.NetworkRegistry;

import buildcraft.core.network.BuildCraftPacket;
import buildcraft.core.network.PacketCoordinates;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketSlotChange;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.gui.ContainerGateInterface;
import buildcraft.transport.pipes.PipeItemsDiamond;
import buildcraft.transport.pipes.PipeItemsEmerald;

@Sharable
public class PacketHandlerTransport extends SimpleChannelInboundHandler<BuildCraftPacket>  {

	/**
	 * TODO: A lot of this is based on the player to retrieve the world.
	 * Passing a dimension id would be more appropriate. More generally, it
	 * seems like a lot of these packets could be replaced with tile-based
	 * RPCs.
	 */
	@Override
	protected  void channelRead0(ChannelHandlerContext ctx, BuildCraftPacket packet) {
		try {
			INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
			EntityPlayer player = CoreProxy.proxy.getPlayerFromNetHandler(netHandler);

			int packetID = packet.getID();

			switch (packetID) {
				case PacketIds.PIPE_POWER:
					onPacketPower(player, (PacketPowerUpdate) packet);
					break;
				case PacketIds.PIPE_LIQUID:
					// action will have happened already at read time
					break;
				case PacketIds.PIPE_TRAVELER: {
					onPipeTravelerUpdate(player, (PacketPipeTransportTraveler) packet);
					break;
				}
				case PacketIds.GATE_ACTIONS:
					onGateActions(player, (PacketUpdate) packet);
					break;
				case PacketIds.GATE_TRIGGERS:
					onGateTriggers(player, (PacketUpdate) packet);
					break;
				case PacketIds.GATE_SELECTION:
					onGateSelection(player, (PacketUpdate) packet);
					break;
				case PacketIds.PIPE_ITEMSTACK: {
					// action will have happened already at read time
					break;
				}
				case PacketIds.PIPE_GATE_EXPANSION_MAP: {
					// action will have happened already at read time
					break;
				}

				/**
				 * SERVER SIDE *
				 */
				case PacketIds.DIAMOND_PIPE_SELECT: {
					onDiamondPipeSelect(player, (PacketSlotChange) packet);
					break;
				}

				case PacketIds.EMERALD_PIPE_SELECT: {
					onEmeraldPipeSelect(player, (PacketSlotChange) packet);
					break;
				}

				case PacketIds.GATE_REQUEST_INIT:
					onGateInitRequest(player, (PacketCoordinates) packet);
					break;

				case PacketIds.GATE_REQUEST_SELECTION:
					onGateSelectionRequest(player, (PacketCoordinates) packet);
					break;

				case PacketIds.GATE_SELECTION_CHANGE:
					onGateSelectionChange(player, (PacketUpdate) packet);
					break;

				case PacketIds.PIPE_ITEMSTACK_REQUEST: {
					((PacketPipeTransportItemStackRequest) packet).sendDataToPlayer(player);
					break;
				}
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

		if (!(container instanceof ContainerGateInterface)) {
			return;
		}

		((ContainerGateInterface) container).updateActions(packet);
	}

	/**
	 * Handles received list of potential triggers on a gate.
	 *
	 * @param packet
	 */
	private void onGateTriggers(EntityPlayer player, PacketUpdate packet) {
		Container container = player.openContainer;

		if (!(container instanceof ContainerGateInterface)) {
			return;
		}

		((ContainerGateInterface) container).updateTriggers(packet);
	}

	/**
	 * Handles received current gate selection on a gate
	 *
	 * @param packet
	 */
	private void onGateSelection(EntityPlayer player, PacketUpdate packet) {
		Container container = player.openContainer;

		if (!(container instanceof ContainerGateInterface)) {
			return;
		}

		((ContainerGateInterface) container).setSelection(packet, false);
	}

	/**
	 * Updates items in a pipe.
	 *
	 * @param packet
	 */
	private void onPipeTravelerUpdate(EntityPlayer player, PacketPipeTransportTraveler packet) {
		World world = player.worldObj;

		if (!world.blockExists(packet.posX, packet.posY, packet.posZ)) {
			return;
		}

		TileEntity entity = world.getTileEntity(packet.posX, packet.posY, packet.posZ);
		if (!(entity instanceof TileGenericPipe)) {
			return;
		}

		TileGenericPipe pipe = (TileGenericPipe) entity;
		if (pipe.pipe == null) {
			return;
		}

		if (!(pipe.pipe.transport instanceof PipeTransportItems)) {
			return;
		}

		((PipeTransportItems) pipe.pipe.transport).handleTravelerPacket(packet);
	}

	/**
	 * Updates the display power on a power pipe
	 *
	 * @param packetPower
	 */
	private void onPacketPower(EntityPlayer player, PacketPowerUpdate packetPower) {
		World world = player.worldObj;
		if (!world.blockExists(packetPower.posX, packetPower.posY, packetPower.posZ)) {
			return;
		}

		TileEntity entity = world.getTileEntity(packetPower.posX, packetPower.posY, packetPower.posZ);
		if (!(entity instanceof TileGenericPipe)) {
			return;
		}

		TileGenericPipe pipe = (TileGenericPipe) entity;
		if (pipe.pipe == null) {
			return;
		}

		if (!(pipe.pipe.transport instanceof PipeTransportPower)) {
			return;
		}

		((PipeTransportPower) pipe.pipe.transport).handlePowerPacket(packetPower);

	}

	/**
	 * ****************** SERVER ******************** *
	 */
	/**
	 * Handles selection changes on a gate.
	 *
	 * @param playerEntity
	 * @param packet
	 */
	private void onGateSelectionChange(EntityPlayer playerEntity, PacketUpdate packet) {
		if (!(playerEntity.openContainer instanceof ContainerGateInterface)) {
			return;
		}

		((ContainerGateInterface) playerEntity.openContainer).setSelection(packet, true);
	}

	/**
	 * Handles gate gui (current) selection requests.
	 *
	 * @param playerEntity
	 * @param packet
	 */
	private void onGateSelectionRequest(EntityPlayer playerEntity, PacketCoordinates packet) {
		if (!(playerEntity.openContainer instanceof ContainerGateInterface)) {
			return;
		}

		((ContainerGateInterface) playerEntity.openContainer).sendSelection(playerEntity);
	}

	/**
	 * Handles received gate gui initialization requests.
	 *
	 * @param playerEntity
	 * @param packet
	 */
	private void onGateInitRequest(EntityPlayer playerEntity, PacketCoordinates packet) {
		if (!(playerEntity.openContainer instanceof ContainerGateInterface)) {
			return;
		}

		((ContainerGateInterface) playerEntity.openContainer).handleInitRequest(playerEntity);
	}

	/**
	 * Retrieves pipe at specified coordinates if any.
	 *
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 */
	private TileGenericPipe getPipe(World world, int x, int y, int z) {
		if (!world.blockExists(x, y, z)) {
			return null;
		}

		TileEntity tile = world.getTileEntity(x, y, z);
		if (!(tile instanceof TileGenericPipe)) {
			return null;
		}

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
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof PipeItemsDiamond)) {
			return;
		}

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
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof PipeItemsEmerald)) {
			return;
		}

		((PipeItemsEmerald) pipe.pipe).getFilters().setInventorySlotContents(packet.slot, packet.stack);
	}
}
