package buildcraft.core.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import buildcraft.transport.Pipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class PacketRPCPipe extends BuildCraftPacket {

	public Pipe pipe;

	byte [] contents;

	public EntityPlayer sender;

	public PacketRPCPipe () {

	}

	public PacketRPCPipe (byte [] bytes) {
		contents = bytes;
	}

	public void setPipe (Pipe aPipe) {
		pipe = aPipe;
	}

	@Override
	public int getID() {
		return PacketIds.RPC_PIPE;
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		RPCMessageInfo info = new RPCMessageInfo();
		info.sender = sender;

		RPCHandler.receiveRPC(pipe, info, data);
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.write(contents);
	}

}
