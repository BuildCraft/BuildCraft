package buildcraft.silicon.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

import buildcraft.core.network.PacketCoordinates;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketSlotChange;
import buildcraft.core.network.PacketUpdate;
import buildcraft.silicon.TileAssemblyAdvancedWorkbench;
import buildcraft.silicon.TileAssemblyTable;
import buildcraft.silicon.TileAssemblyTable.SelectionMessage;
import buildcraft.silicon.gui.ContainerAssemblyTable;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
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
				onSelectionUpdate((EntityPlayer)player, packetT);
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
			case PacketIds.ADVANCED_WORKBENCH_SETSLOT:
				PacketSlotChange packet1 = new PacketSlotChange();
				packet1.readData(data);
				onAdvancedWorkbenchSet((EntityPlayer) player, packet1);
				break;

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void onSelectionUpdate(EntityPlayer player, PacketUpdate packet) {

		Container container = player.craftingInventory;

		if (container instanceof ContainerAssemblyTable) {
			SelectionMessage message = new SelectionMessage();
			TileAssemblyTable.selectionMessageWrapper.fromPayload(message, packet.payload);
			((ContainerAssemblyTable)container).handleSelectionMessage(message);
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

	private TileAssemblyAdvancedWorkbench getAdvancedWorkbench(World world, int x, int y, int z) {

		if (!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if (!(tile instanceof TileAssemblyAdvancedWorkbench))
			return null;

		return (TileAssemblyAdvancedWorkbench) tile;
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

	/**
	 * Sets the packet into the advanced workbench
	 *
	 * @param player
	 * @param packet1
	 */
	private void onAdvancedWorkbenchSet(EntityPlayer player, PacketSlotChange packet1) {

		TileAssemblyAdvancedWorkbench tile = getAdvancedWorkbench(player.worldObj, packet1.posX, packet1.posY, packet1.posZ);
		if (tile == null)
			return;

		tile.updateCraftingMatrix(packet1.slot, packet1.stack);
	}
}
