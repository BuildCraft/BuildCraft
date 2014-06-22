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
import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.EntityPlayer;

public class PacketRPCStatic extends PacketRPC {
	private byte[] contents;
	private Class<?> clas;

	public PacketRPCStatic() {
	}

	public PacketRPCStatic(Class iClass, byte[] bytes) {
		contents = bytes;
		clas = iClass;
	}

	@Override
	public void readData(ByteBuf data) {
		contents = new byte [data.readableBytes()];
		data.readBytes(contents);
	}

	@Override
	public void call (EntityPlayer sender) {
		RPCMessageInfo info = new RPCMessageInfo();
		info.sender = sender;

		ByteBuf completeData = Unpooled.buffer();
		completeData.writeBytes(contents);

		String classId = NetworkIdRegistry.read(completeData);

		try {
			RPCHandler.receiveStaticRPC(Class.forName(classId), info, completeData);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void writeData(ByteBuf data) {
		NetworkIdRegistry.write(data, clas.getCanonicalName());
		data.writeBytes(contents);
	}
}
