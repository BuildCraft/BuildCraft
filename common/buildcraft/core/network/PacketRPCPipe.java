/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;

import buildcraft.transport.Pipe;

// TODO: This is not yet used
public class PacketRPCPipe extends BuildCraftPacket {

	public Pipe pipe;
	public EntityPlayer sender;

	private byte[] contents;

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
