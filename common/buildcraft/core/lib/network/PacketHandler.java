/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.network;

import java.io.IOException;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.NetworkRegistry;

import buildcraft.api.core.ISerializable;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.network.PacketIds;
import buildcraft.core.proxy.CoreProxy;

@Sharable
public class PacketHandler extends SimpleChannelInboundHandler<Packet> {
	private void onTileUpdate(EntityPlayer player, PacketTileUpdate packet) throws IOException {
		World world = player.worldObj;

		if (!packet.targetExists(world)) {
			return;
		}

		TileEntity entity = packet.getTarget(world);

		if (!(entity instanceof ISerializable)) {
			return;
		}

		ISerializable tile = (ISerializable) entity;
		tile.readData(packet.stream);
	}

	private void onEntityUpdate(EntityPlayer player, PacketEntityUpdate packet) throws IOException {
		World world = player.worldObj;

		if (!packet.targetExists(world)) {
			return;
		}

		Entity entity = packet.getTarget(world);

		if (!(entity instanceof ISerializable)) {
			return;
		}

		ISerializable payload = (ISerializable) entity;
		payload.readData(packet.stream);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
		try {
			INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
			EntityPlayer player = CoreProxy.proxy.getPlayerFromNetHandler(netHandler);

			int packetID = packet.getID();

			switch (packetID) {
				case PacketIds.TILE_UPDATE: {
					onTileUpdate(player, (PacketTileUpdate) packet);
					break;
				}

				case PacketIds.ENTITY_UPDATE: {
					onEntityUpdate(player, (PacketEntityUpdate) packet);
					break;
				}

				case PacketIds.COMMAND: {
					((PacketCommand) packet).handle(player);
					break;
				}

				case PacketIds.STATE_UPDATE: {
					PacketTileState pkt = (PacketTileState) packet;
					World world = player.worldObj;

					TileEntity tile = world.getTileEntity(pkt.posX, pkt.posY, pkt.posZ);

					if (tile instanceof ISyncedTile) {
						pkt.applyStates((ISyncedTile) tile);
					}

					break;
				}

				case PacketIds.GUI_RETURN: {
					// action will have happened already at read time
					break;
				}

				case PacketIds.GUI_WIDGET: {
					// action will have happened already at read time
					break;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
