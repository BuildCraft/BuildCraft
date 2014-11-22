/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.utils;

import io.netty.buffer.ByteBuf;

import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.core.ISerializable;

public class RobotStationMatrix implements ISerializable {

	// TODO: All these matrixes should be passed by RPC, instead of having a
	// single state carrying everything

	private RobotStationState[] states = new RobotStationState[6];

	private boolean dirty = false;

	public RobotStationMatrix() {
		for (int i = 0; i < states.length; ++i) {
			states[i] = RobotStationState.None;
		}
	}

	public boolean isConnected(ForgeDirection direction) {
		return states[direction.ordinal()] != RobotStationState.None;
	}

	public void setState(ForgeDirection direction, RobotStationState value) {
		if (states[direction.ordinal()] != value) {
			states[direction.ordinal()] = value;
			dirty = true;
		}
	}

	public RobotStationState getState(ForgeDirection direction) {
		return states[direction.ordinal()];
	}

	public boolean isDirty() {
		return dirty;
	}

	public void clean() {
		dirty = false;
	}

	@Override
	public void writeData(ByteBuf data) {
		for (int i = 0; i < states.length; i++) {
			data.writeByte(states[i].ordinal());
		}
	}

	@Override
	public void readData(ByteBuf data) {
		for (int i = 0; i < states.length; i++) {
			states[i] = RobotStationState.values()[data.readUnsignedByte()];
		}
	}
}
