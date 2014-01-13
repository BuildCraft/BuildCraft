package buildcraft.factory.network;

import buildcraft.core.network.BuildCraftChannelHandler;
import buildcraft.core.network.BuildCraftPacket;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketPayloadStream;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.factory.TileRefinery;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;

public class PacketHandlerFactory extends BuildCraftChannelHandler {

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf data, BuildCraftPacket packet) {
		super.decodeInto(ctx, data, packet);
		
		try {
			INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
			
			EntityPlayer player = 
					CoreProxy.proxy.getPlayerFromNetHandler(netHandler);
			
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

	private TileRefinery getRefinery(World world, int x, int y, int z) {
		if (!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if (!(tile instanceof TileRefinery))
			return null;

		return (TileRefinery) tile;
	}

	private void onRefinerySelect(EntityPlayer playerEntity, PacketUpdate packet) throws IOException {

		TileRefinery tile = getRefinery(playerEntity.worldObj, packet.posX, packet.posY, packet.posZ);
		if (tile == null || packet.payload == null)
			return;
	
		DataInputStream stream = ((PacketPayloadStream)packet.payload).stream;

		tile.setFilter(stream.readByte(), FluidRegistry.getFluid(stream.readShort()));
	}
}
