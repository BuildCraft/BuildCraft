/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory.network;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.NetworkRegistry;

import net.minecraftforge.fluids.FluidRegistry;

import buildcraft.core.network.BuildCraftPacket;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.factory.TileRefinery;

@Sharable
public class PacketHandlerFactory extends SimpleChannelInboundHandler<BuildCraftPacket> {

	@Override
	protected  void channelRead0(ChannelHandlerContext ctx, BuildCraftPacket packet) {
		try {
			INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
			EntityPlayer player = CoreProxy.proxy.getPlayerFromNetHandler(netHandler);

			int packetID = packet.getID();

			switch (packetID) {

				case PacketIds.REFINERY_FILTER_SET:
					onRefinerySelect(player, (PacketUpdate) packet);
					break;

			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private TileRefinery getRefinery(World world, BlockPos pos) {
		if (!world.isBlockLoaded(pos)) {
			return null;
		}

		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof TileRefinery)) {
			return null;
		}

		return (TileRefinery) tile;
	}

	private void onRefinerySelect(EntityPlayer playerEntity, PacketUpdate packet) throws IOException {

		TileRefinery tile = getRefinery(playerEntity.worldObj, packet.pos);
		if (tile == null || packet.payload == null) {
			return;
		}

		ByteBuf stream = packet.stream;

		tile.setFilter(stream.readByte(), FluidRegistry.getFluid(stream.readShort()));
	}
}
