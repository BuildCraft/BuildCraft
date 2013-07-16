package buildcraft.builders.network;

import buildcraft.builders.TileArchitect;
import buildcraft.builders.TileBlueprintLibrary;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketPayloadArrays;
import buildcraft.core.network.PacketUpdate;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;

public class PacketHandlerBuilders implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {

		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		try {
			int packetID = data.read();
			switch (packetID) {
			case PacketIds.ARCHITECT_NAME:
				PacketUpdate packetA = new PacketUpdate();
				packetA.readData(data);
				onArchitectName((EntityPlayer) player, packetA);
				break;
			case PacketIds.LIBRARY_ACTION:
				PacketLibraryAction packetB = new PacketLibraryAction();
				packetB.readData(data);
				onLibraryAction((EntityPlayer) player, packetB);
				break;
			case PacketIds.LIBRARY_SELECT:
				PacketLibraryAction packetC = new PacketLibraryAction();
				packetC.readData(data);
				onLibrarySelect((EntityPlayer) player, packetC);
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
