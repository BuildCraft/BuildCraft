/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.network;

import buildcraft.core.network.PacketCoordinates;
import buildcraft.core.network.PacketIds;
import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketPowerUpdate extends PacketCoordinates {

	public boolean overload;
	public short[] displayPower;

	public PacketPowerUpdate() {
	}

	public PacketPowerUpdate(int x, int y, int z) {
		super(PacketIds.PIPE_POWER, x, y, z);
	}

	@Override
	public void readData(ByteBuf data) {		
		super.readData(data);
		displayPower = new short[] { 0, 0, 0, 0, 0, 0 };
		overload = data.readBoolean();
		for (int i = 0; i < displayPower.length; i++) {
			displayPower[i] = data.readByte();
		}
	}

	@Override
	public void writeData(ByteBuf data) {
		super.writeData(data);
		data.writeBoolean(overload);
		for (int i = 0; i < displayPower.length; i++) {
			data.writeByte(displayPower[i]);
		}
	}
}
