package buildcraft.core.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import buildcraft.core.proxy.CoreProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.NetworkRegistry;

public class PacketHandler extends BuildCraftChannelHandler {

	private void onTileUpdate(EntityPlayer player, PacketTileUpdate packet) throws IOException {
		World world = player.worldObj;

		if (!packet.targetExists(world))
			return;

		TileEntity entity = packet.getTarget(world);
		if (!(entity instanceof ISynchronizedTile))
			return;

		ISynchronizedTile tile = (ISynchronizedTile) entity;
		tile.handleUpdatePacket(packet);
		tile.postPacketHandling(packet);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf data, BuildCraftPacket packet) {
		super.decodeInto(ctx, data, packet);
		
		try {

			INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
			
			EntityPlayer player = 
					CoreProxy.proxy.getPlayerFromNetHandler(netHandler);

			int packetID = packet.getID();
			
			switch (packetID) {
				case PacketIds.TILE_UPDATE: {
					onTileUpdate(player, (PacketTileUpdate) packet);
					break;
				}

				case PacketIds.STATE_UPDATE: {
					PacketTileState pkt = (PacketTileState) packet;
					pkt.readData(data);
					World world = player.worldObj;
					TileEntity tile = world.getBlockTileEntity(pkt.posX, pkt.posY, pkt.posZ);
					if (tile instanceof ISyncedTile) {
						pkt.applyStates(data, (ISyncedTile) tile);
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
