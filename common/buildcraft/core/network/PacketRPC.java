package buildcraft.core.network;

import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class PacketRPC extends BuildCraftPacket {

	public TileEntity tile;

	byte [] contents;

	public EntityPlayer sender;

	public PacketRPC () {

	}

	public PacketRPC (byte [] bytes) {
		contents = bytes;
	}

	public void setTile (TileEntity aTile) {
		tile = aTile;
	}

	@Override
	public int getID() {
		return PacketIds.RPC;
	}

	@Override
	public void readData(ByteBuf data) {
		RPCMessageInfo info = new RPCMessageInfo();
		info.sender = sender;

		RPCHandler.receiveRPC(tile, info, data);
	}

	@Override
	public void writeData(ByteBuf data) {
		data.writeBytes(contents);
	}

}
