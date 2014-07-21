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

public class PacketRPCStatic extends PacketRPC {
	private String classId;
	private Class<?> clas;

	public PacketRPCStatic() {
	}

	public PacketRPCStatic(Class iClass, ByteBuf bytes) {
		contents = bytes;
		clas = iClass;
	}

	@Override
	public void readData(ByteBuf data) {
		super.readData(data);

		classId = NetworkIdRegistry.read(data);
	}

	@Override
	public void writeData(ByteBuf data) {
		super.writeData(data);

		NetworkIdRegistry.write(data, clas.getCanonicalName());
	}

	@Override
	public void call (EntityPlayer sender) {
		super.call(sender);

		RPCMessageInfo info = new RPCMessageInfo();
		info.sender = sender;

		try {
			RPCHandler.receiveStaticRPC(Class.forName(classId), info, contents);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
