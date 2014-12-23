/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.network;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import buildcraft.core.network.BuildCraftPacket;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketSlotChange;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.silicon.TileAdvancedCraftingTable;
import buildcraft.silicon.TileAssemblyTable;

@Sharable
public class PacketHandlerSilicon extends SimpleChannelInboundHandler<BuildCraftPacket>  {

	@Override
	protected  void channelRead0(ChannelHandlerContext ctx, BuildCraftPacket packet) {
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

	private TileAssemblyTable getAssemblyTable(World world, BlockPos pos) {

		if (world.isAirBlock(pos)) {
			return null;
		}

		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof TileAssemblyTable)) {
			return null;
		}

		return (TileAssemblyTable) tile;
	}

	private TileAdvancedCraftingTable getAdvancedWorkbench(World world, BlockPos pos) {

		if (world.isAirBlock(pos)) {
			return null;
		}

		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof TileAdvancedCraftingTable)) {
			return null;
		}

		return (TileAdvancedCraftingTable) tile;
	}

	/**
	 * Sets the packet into the advanced workbench
	 *
	 * @param player
	 * @param packet1
	 */
	private void onAdvancedWorkbenchSet(EntityPlayer player, PacketSlotChange packet1) {

		TileAdvancedCraftingTable tile = getAdvancedWorkbench(player.worldObj, packet1.pos);
		if (tile == null) {
			return;
		}

		tile.updateCraftingMatrix(packet1.slot, packet1.stack);
	}
}
