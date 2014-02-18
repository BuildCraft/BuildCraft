/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.tileentity.TileEntity;
import buildcraft.builders.TileArchitect;
import buildcraft.builders.TileBlueprintLibrary;
import buildcraft.core.network.BuildCraftChannelHandler;
import buildcraft.core.network.BuildCraftPacket;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;
import cpw.mods.fml.common.network.NetworkRegistry;

public class PacketHandlerBuilders extends BuildCraftChannelHandler {

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf data, BuildCraftPacket packet) {
		super.decodeInto(ctx, data, packet);

		try {
			INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
			EntityPlayer player = CoreProxy.proxy.getPlayerFromNetHandler(netHandler);

			int packetID = packet.getID();

			switch (packetID) {
			// FIXME: Replace that by a RPC
			case PacketIds.ARCHITECT_NAME:
				onArchitectName(player, (PacketUpdate) packet);
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void onArchitectName(EntityPlayer player, PacketUpdate packet) {
		// FIXME: Not much point to port this to stream payload at this stage,
		// should use a RPC instead.
		/*TileEntity te = player.worldObj.getBlockTileEntity(packet.posX, packet.posY, packet.posZ);
		if (te instanceof TileArchitect) {
			((TileArchitect) te).handleClientInput((char) ((PacketPayloadArrays)packet.payload).intPayload[0]);
		}*/
	}

}
