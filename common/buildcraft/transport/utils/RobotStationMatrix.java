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

import buildcraft.api.core.NetworkData;
import buildcraft.core.network.serializers.ClassMapping;
import buildcraft.core.network.serializers.SerializationContext;

public class RobotStationMatrix {
	public enum State {
		None,
		Available,
		Reserved,
		Linked
	}

	// TODO: All these matrixes should be passed by RPC, instead of having a
	// single state carrying everything

	@NetworkData
	private State[] states = new State[6];

	private boolean dirty = false;

	public boolean isConnected(ForgeDirection direction) {
		return states[direction.ordinal()] == State.None;
	}

	public void setState(ForgeDirection direction, State value) {
		if (states[direction.ordinal()] != value) {
			states[direction.ordinal()] = value;
			dirty = true;
		}
	}

	public boolean isDirty() {
		return dirty;
	}

	public void clean() {
		dirty = false;
	}

	public void writeData(ByteBuf data) {
		try {
			SerializationContext context = new SerializationContext();
			ClassMapping.get(this.getClass()).write(data, this, context);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void readData(ByteBuf data) {
		try {
			SerializationContext context = new SerializationContext();
			ClassMapping.get(this.getClass()).read(data, this, context);
			dirty = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
