package buildcraft.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.Pipe;

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
	public void readData(ByteBuf data) {
		RPCMessageInfo info = new RPCMessageInfo();
		info.sender = sender;

		RPCHandler.receiveRPC(pipe, info, data);
	}

	@Override
	public void writeData(ByteBuf data) {
		data.writeBytes(contents);
	}

}
