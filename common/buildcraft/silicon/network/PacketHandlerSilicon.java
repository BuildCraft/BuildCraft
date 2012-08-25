package buildcraft.silicon.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

import buildcraft.core.network.PacketCoordinates;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketUpdate;
import buildcraft.factory.TileAssemblyTable;
import buildcraft.factory.TileAssemblyTable.SelectionMessage;
import buildcraft.silicon.gui.GuiAssemblyTable;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class PacketHandlerSilicon implements IPacketHandler {

	@Override
	public void onPacketData(NetworkManager manager, Packet250CustomPayload packet, Player player) {

		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		try {
			int packetID = data.read();
			switch (packetID) {
			case PacketIds.SELECTION_ASSEMBLY_SEND:
				PacketUpdate packetT = new PacketUpdate();
				packetT.readData(data);
				onSelectionUpdate(packetT);
				break;

			case PacketIds.SELECTION_ASSEMBLY:
				PacketUpdate packetA = new PacketUpdate();
				packetA.readData(data);
				onAssemblySelect((EntityPlayer)player, packetA);
				break;
			case PacketIds.SELECTION_ASSEMBLY_GET:
				PacketCoordinates packetC = new PacketCoordinates();
				packetC.readData(data);
				onAssemblyGetSelection((EntityPlayer)player, packetC);
				break;

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void onSelectionUpdate(PacketUpdate packet) {

		GuiScreen screen = FMLClientHandler.instance().getClient().currentScreen;

		if (screen instanceof GuiAssemblyTable) {
			GuiAssemblyTable gui = (GuiAssemblyTable) screen;
			SelectionMessage message = new SelectionMessage();

			TileAssemblyTable.selectionMessageWrapper.fromPayload(message, packet.payload);
			gui.handleSelectionMessage(message);
		}
	}


	private TileAssemblyTable getAssemblyTable(World world, int x, int y, int z) {

		if (!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if (!(tile instanceof TileAssemblyTable))
			return null;

		return (TileAssemblyTable) tile;
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
	 * @param packet
	 */
	private void onAssemblySelect(EntityPlayer player, PacketUpdate packet) {

		TileAssemblyTable tile = getAssemblyTable(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (tile == null)
			return;

		TileAssemblyTable.SelectionMessage message = new TileAssemblyTable.SelectionMessage();
		TileAssemblyTable.selectionMessageWrapper.fromPayload(message, packet.payload);
		tile.handleSelectionMessage(message);
	}

}
