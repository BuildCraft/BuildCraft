package buildcraft.silicon.network;

import buildcraft.core.network.PacketCoordinates;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketNBT;
import buildcraft.core.network.PacketSlotChange;
import buildcraft.silicon.TileAdvancedCraftingTable;
import buildcraft.silicon.TileAssemblyTable;
import buildcraft.silicon.TileAssemblyTable.SelectionMessage;
import buildcraft.silicon.gui.ContainerAssemblyTable;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PacketHandlerSilicon implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {

		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		try {
			int packetID = data.read();
			switch (packetID) {
			case PacketIds.SELECTION_ASSEMBLY_SEND:
				PacketNBT packetT = new PacketNBT();
				packetT.readData(data);
				onSelectionUpdate((EntityPlayer) player, packetT);
				break;

			case PacketIds.SELECTION_ASSEMBLY:
				PacketNBT packetA = new PacketNBT();
				packetA.readData(data);
				onAssemblySelect((EntityPlayer) player, packetA);
				break;
			case PacketIds.SELECTION_ASSEMBLY_GET:
				PacketCoordinates packetC = new PacketCoordinates();
				packetC.readData(data);
				onAssemblyGetSelection((EntityPlayer) player, packetC);
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

		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if (!(tile instanceof TileAssemblyTable))
			return null;

		return (TileAssemblyTable) tile;
	}

	private TileAdvancedCraftingTable getAdvancedWorkbench(World world, int x, int y, int z) {

		if (!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getBlockTileEntity(x, y, z);
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
