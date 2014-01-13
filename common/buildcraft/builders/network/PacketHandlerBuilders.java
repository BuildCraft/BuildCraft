package buildcraft.builders.network;

import buildcraft.builders.TileArchitect;
import buildcraft.builders.TileBlueprintLibrary;
import buildcraft.core.network.BuildCraftChannelHandler;
import buildcraft.core.network.BuildCraftPacket;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketPayloadArrays;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.Player;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;

public class PacketHandlerBuilders extends BuildCraftChannelHandler {

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf data, BuildCraftPacket packet) {
		super.decodeInto(ctx, data, packet);

		try {
			INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
			
			EntityPlayer player = 
					CoreProxy.proxy.getPlayerFromNetHandler(netHandler);

			int packetID = packet.getID();
			
			switch (packetID) {
			case PacketIds.ARCHITECT_NAME:
				onArchitectName(player, (PacketUpdate) packet);
				break;
			case PacketIds.LIBRARY_ACTION:
				onLibraryAction(player, (PacketLibraryAction) packet);
				break;
			case PacketIds.LIBRARY_SELECT:
				onLibrarySelect(player, (PacketLibraryAction) packet);
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void onArchitectName(EntityPlayer player, PacketUpdate packet) {
		TileEntity te = player.worldObj.getBlockTileEntity(packet.posX, packet.posY, packet.posZ);
		if (te instanceof TileArchitect) {
			((TileArchitect) te).handleClientInput((char) ((PacketPayloadArrays)packet.payload).intPayload[0]);
		}
	}

	private void onLibraryAction(EntityPlayer player, PacketLibraryAction packet) {
		TileEntity te = player.worldObj.getBlockTileEntity(packet.posX, packet.posY, packet.posZ);
		if (te instanceof TileBlueprintLibrary) {
			TileBlueprintLibrary tbl = (TileBlueprintLibrary) te;
			switch (packet.actionId) {
			case TileBlueprintLibrary.COMMAND_DELETE:
				tbl.deleteSelectedBpt();
				break;
			case TileBlueprintLibrary.COMMAND_LOCK_UPDATE:
				tbl.locked = !tbl.locked;
				tbl.sendNetworkUpdate();
				break;
			case TileBlueprintLibrary.COMMAND_NEXT:
				tbl.setCurrentPage(true);
				break;
			case TileBlueprintLibrary.COMMAND_PREV:
				tbl.setCurrentPage(false);
				break;
			}
		}
	}

	private void onLibrarySelect(EntityPlayer player, PacketLibraryAction packet) {
		TileEntity te = player.worldObj.getBlockTileEntity(packet.posX, packet.posY, packet.posZ);
		if (te instanceof TileBlueprintLibrary) {
			TileBlueprintLibrary tbl = (TileBlueprintLibrary) te;
			int ySlot = packet.actionId;
			if (ySlot < tbl.getCurrentPage().size()) {
				tbl.selected = ySlot;
			}
			tbl.sendNetworkUpdate();
		}
	}

}
