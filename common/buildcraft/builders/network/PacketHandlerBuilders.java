package buildcraft.builders.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

import buildcraft.builders.TileArchitect;
import buildcraft.builders.TileBlueprintLibrary;
import buildcraft.core.network.PacketCoordinates;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.factory.TileAssemblyTable;
import buildcraft.factory.TileAssemblyTable.SelectionMessage;
import buildcraft.silicon.gui.ContainerAssemblyTable;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class PacketHandlerBuilders implements IPacketHandler {

	@Override
	public void onPacketData(NetworkManager manager, Packet250CustomPayload packet, Player player) {

		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		try {
			int packetID = data.read();
			switch (packetID) {
			case PacketIds.ARCHITECT_NAME:
				PacketUpdate packetA = new PacketUpdate();
				packetA.readData(data);
				onArchitectName((EntityPlayer)player, packetA);
				break;
			case PacketIds.LIBRARY_COMMAND:
				PacketUpdate packetB = new PacketUpdate();
				packetB.readData(data);
				onLibraryCommand((EntityPlayer)player, packetB);
				break;
			case PacketIds.LIBRARY_LOCK_UPDATE:
				PacketCoordinates packetC = new PacketCoordinates();
				packetC.readData(data);
				onLockUpdate((EntityPlayer)player, packetC);
				break;
			case PacketIds.LIBRARY_DELETE:
				PacketCoordinates packetD = new PacketCoordinates();
				packetD.readData(data);
				onDeleteBpt((EntityPlayer)player, packetD);
				break;
			case PacketIds.LIBRARY_SELECT:
				PacketUpdate packetE = new PacketUpdate();
				packetE.readData(data);
				onLibrarySelect((EntityPlayer)player, packetE);
				break;

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void onArchitectName(EntityPlayer player, PacketUpdate packet) {
		TileEntity te = player.worldObj.getBlockTileEntity(packet.posX,
				packet.posY, packet.posZ);
		if(te instanceof TileArchitect){
			((TileArchitect) te).handleClientInput((char) packet.payload.intPayload[0]);
		}
	}

	private void onLibraryCommand(EntityPlayer player, PacketUpdate packet) {
		TileEntity te = player.worldObj.getBlockTileEntity(packet.posX,
				packet.posY, packet.posZ);
		if(te instanceof TileArchitect) {
			TileBlueprintLibrary tbl = (TileBlueprintLibrary) te;
			boolean nextPage = packet.payload.intPayload[0] == 0;
			if (nextPage) {
				if (tbl.getCurrentPage().size() > 0) {
					tbl.setCurrentPage(tbl.getNextPage(tbl.getCurrentPage().get(tbl.currentNames.length - 1).file.getName()));
				} else {
					tbl.setCurrentPage(tbl.getNextPage(null));
				}
			} else {
				if (tbl.getCurrentPage().size() > 0) {
					tbl.setCurrentPage(tbl.getPrevPage(tbl.getCurrentPage().get(0).file.getName()));
				} else {
					tbl.setCurrentPage(tbl.getNextPage(null));
				}
			}
			tbl.sendNetworkUpdate();
		}
	}

	private void onLibrarySelect(EntityPlayer player, PacketUpdate packet) {
		TileEntity te = player.worldObj.getBlockTileEntity(packet.posX,
				packet.posY, packet.posZ);
		if(te instanceof TileBlueprintLibrary){
			TileBlueprintLibrary tbl = (TileBlueprintLibrary) te;
			int ySlot = packet.payload.intPayload[0];
			if (ySlot < tbl.currentNames.length){
				tbl.selected = ySlot;
			}
			tbl.sendNetworkUpdate();
		}
	}

	private void onLockUpdate(EntityPlayer player, PacketCoordinates packet) {
		TileEntity te = player.worldObj.getBlockTileEntity(packet.posX,
				packet.posY, packet.posZ);
		if(te instanceof TileBlueprintLibrary){
			TileBlueprintLibrary tbl = (TileBlueprintLibrary) te;
			tbl.locked = !tbl.locked;
			tbl.sendNetworkUpdate();
		}
	}

	private void onDeleteBpt(EntityPlayer player, PacketCoordinates packet) {
		TileEntity te = player.worldObj.getBlockTileEntity(packet.posX,
				packet.posY, packet.posZ);
		if(te instanceof TileBlueprintLibrary){
			TileBlueprintLibrary tbl = (TileBlueprintLibrary) te;
			tbl.deleteSelectedBpt(player);
			tbl.sendNetworkUpdate();
		}
	}
	
	
}
