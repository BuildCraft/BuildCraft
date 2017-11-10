/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.network;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.NetworkRegistry;

import buildcraft.core.lib.network.Packet;
import buildcraft.core.lib.network.PacketHandler;
import buildcraft.core.lib.network.PacketSlotChange;
import buildcraft.core.network.PacketIds;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.silicon.TileAdvancedCraftingTable;

@Sharable
public class PacketHandlerSilicon extends PacketHandler {
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
		super.channelRead0(ctx, packet);
		try {
			INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
			EntityPlayer player = CoreProxy.proxy.getPlayerFromNetHandler(netHandler);

			int packetID = packet.getID();

			switch (packetID) {
				case PacketIds.ADVANCED_WORKBENCH_SETSLOT:
					onAdvancedWorkbenchSet(player, (PacketSlotChange) packet);
					break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private TileAdvancedCraftingTable getAdvancedWorkbench(World world, int x, int y, int z) {
		if (!world.blockExists(x, y, z)) {
			return null;
		}

		TileEntity tile = world.getTileEntity(x, y, z);
		if (!(tile instanceof TileAdvancedCraftingTable)) {
			return null;
		}

		return (TileAdvancedCraftingTable) tile;
	}

	private void onAdvancedWorkbenchSet(EntityPlayer player, PacketSlotChange packet1) {
		TileAdvancedCraftingTable tile = getAdvancedWorkbench(player.worldObj, packet1.posX, packet1.posY, packet1.posZ);
		if (tile == null) {
			return;
		}

		tile.updateCraftingMatrix(packet1.slot, packet1.stack);
	}
}
