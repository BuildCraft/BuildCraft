/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network;

import java.io.IOException;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.NetworkRegistry;

import buildcraft.core.proxy.CoreProxy;

@Sharable
public class PacketHandler extends SimpleChannelInboundHandler<BuildCraftPacket>  {
	private void onTileUpdate(EntityPlayer player, PacketTileUpdate packet) throws IOException {
		World world = player.worldObj;

		if (!packet.targetExists(world)) {
			return;
		}

		TileEntity entity = packet.getTarget(world);

		if (!(entity instanceof ISynchronizedTile)) {
			return;
		}

		ISynchronizedTile tile = (ISynchronizedTile) entity;
		tile.handleUpdatePacket(packet);
		tile.postPacketHandling(packet);
	}

	@Override
	protected  void channelRead0(ChannelHandlerContext ctx, BuildCraftPacket packet) {
		try {
			INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
			EntityPlayer player = CoreProxy.proxy.getPlayerFromNetHandler(netHandler);

			int packetID = packet.getID();

			switch (packetID) {
				case PacketIds.TILE_UPDATE: {
					onTileUpdate(player, (PacketTileUpdate) packet);
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

				case PacketIds.RPC_TILE: {
					((PacketRPCTile) packet).call(player);

					break;
				}

				case PacketIds.RPC_PIPE: {
				// TODO: RPC pipes are not used right now. Ressurect this
				// code if needed later.
				/*
				 * PacketRPCPipe rpc = new PacketRPCPipe(); rpc.sender = player;
				 *
				 * int dimId = data.readShort(); World world = null;
				 *
				 * if (!rpc.sender.worldObj.isRemote) { // if this is a server,
				 * then get the world
				 *
				 * world = DimensionManager.getProvider(dimId).worldObj; } else
				 * if (rpc.sender.worldObj.provider.dimensionId == dimId) { //
				 * if the player is on this world, then synchronize things
				 *
				 * world = rpc.sender.worldObj; }
				 *
				 * if (world != null) { int x = data.readInt(); int y =
				 * data.readInt(); int z = data.readInt();
				 *
				 * TileEntity tile = world.getTileEntity(x, y, z);
				 *
				 * if (tile instanceof TileGenericPipe) { rpc.setPipe
				 * (((TileGenericPipe) tile).pipe); rpc.readData(data); } }
				 */
				break;

				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}
