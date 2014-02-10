package buildcraft.silicon.network;

import buildcraft.core.network.BuildCraftChannelHandler;
import buildcraft.core.network.BuildCraftPacket;
import buildcraft.core.network.PacketCoordinates;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketNBT;
import buildcraft.core.network.PacketSlotChange;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;
import buildcraft.silicon.TileAdvancedCraftingTable;
import buildcraft.silicon.TileAssemblyTable;
import buildcraft.silicon.TileAssemblyTable.SelectionMessage;
import buildcraft.silicon.gui.ContainerAssemblyTable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.INetHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PacketHandlerSilicon extends BuildCraftChannelHandler {

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf data, BuildCraftPacket packet) {
		super.decodeInto(ctx, data, packet);
				
		try {
			INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
			
			EntityPlayer player = Utils.getPlayerFromNetHandler(netHandler);
			
			int packetID = packet.getID();
			
			switch (packetID) {
			case PacketIds.SELECTION_ASSEMBLY_SEND:			
				onSelectionUpdate(player, (PacketNBT) packet);
				break;
			case PacketIds.SELECTION_ASSEMBLY:
				onAssemblySelect(player, (PacketNBT) packet);
				break;
			case PacketIds.SELECTION_ASSEMBLY_GET:
				onAssemblyGetSelection(player, (PacketCoordinates) packet);
				break;
			case PacketIds.ADVANCED_WORKBENCH_SETSLOT:
				onAdvancedWorkbenchSet(player, (PacketSlotChange) packet);
				break;

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void onSelectionUpdate(EntityPlayer player, PacketNBT packet) {

		Container container = player.openContainer;

		if (container instanceof ContainerAssemblyTable) {
			SelectionMessage message = new SelectionMessage();
			message.fromNBT(packet.getTagCompound());
			((ContainerAssemblyTable) container).handleSelectionMessage(message);
		}
	}

	private TileAssemblyTable getAssemblyTable(World world, int x, int y, int z) {

		if (!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getTileEntity(x, y, z);
		if (!(tile instanceof TileAssemblyTable))
			return null;

		return (TileAssemblyTable) tile;
	}

	private TileAdvancedCraftingTable getAdvancedWorkbench(World world, int x, int y, int z) {

		if (!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getTileEntity(x, y, z);
		if (!(tile instanceof TileAdvancedCraftingTable))
			return null;

		return (TileAdvancedCraftingTable) tile;
	}

	/**
	 * Sends the current selection on the assembly table to a player.
	 * 
	 * @param player
	 * @param packet
	 */
	private void onAssemblyGetSelection(EntityPlayer player, PacketCoordinates packet) {

		TileAssemblyTable tile = getAssemblyTable(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (tile == null)
			return;

		tile.sendSelectionTo(player);
	}

	/**
	 * Sets the selection on an assembly table according to player request.
	 * 
	 * @param player
	 * @param packetA
	 */
	private void onAssemblySelect(EntityPlayer player, PacketNBT packetA) {

		TileAssemblyTable tile = getAssemblyTable(player.worldObj, packetA.posX, packetA.posY, packetA.posZ);
		if (tile == null)
			return;

		TileAssemblyTable.SelectionMessage message = new TileAssemblyTable.SelectionMessage();
		message.fromNBT(packetA.getTagCompound());
		tile.handleSelectionMessage(message);
	}

	/**
	 * Sets the packet into the advanced workbench
	 * 
	 * @param player
	 * @param packet1
	 */
	private void onAdvancedWorkbenchSet(EntityPlayer player, PacketSlotChange packet1) {

		TileAdvancedCraftingTable tile = getAdvancedWorkbench(player.worldObj, packet1.posX, packet1.posY, packet1.posZ);
		if (tile == null)
			return;

		tile.updateCraftingMatrix(packet1.slot, packet1.stack);
	}
}
