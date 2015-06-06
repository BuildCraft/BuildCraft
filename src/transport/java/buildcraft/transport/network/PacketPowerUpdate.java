/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.network;

import io.netty.buffer.ByteBuf;

import buildcraft.core.lib.network.PacketCoordinates;
import buildcraft.core.network.PacketIds;
import buildcraft.transport.render.PipeRendererTESR;

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
			displayPower[i] = data.readUnsignedByte();
		}
	}

	@Override
	public void writeData(ByteBuf data) {
		super.writeData(data);
		data.writeBoolean(overload);
		for (short element : displayPower) {
			data.writeByte(Math.min(PipeRendererTESR.POWER_STAGES,
					(int) Math.ceil(element * PipeRendererTESR.DISPLAY_MULTIPLIER)));
		}
	}
}
