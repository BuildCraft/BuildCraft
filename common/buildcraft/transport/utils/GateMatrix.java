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
import net.minecraft.util.EnumFacing;

public class GateMatrix {

	private final boolean[] isGateLit = new boolean[EnumFacing.values().length];
	private final boolean[] isGatePulsing = new boolean[EnumFacing.values().length];
	private final boolean[] isGateExists = new boolean[EnumFacing.values().length];
	private final int[] gateIconIndex = new int[EnumFacing.values().length];
	private boolean dirty = false;

	public GateMatrix() {
	}

	public boolean isDirty() {
		return dirty;
	}

	public void clean() {
		dirty = false;
	}

	public void setIsGateLit(boolean gateLit, EnumFacing direction) {
		if (isGateLit[direction.ordinal()] != gateLit) {
			isGateLit[direction.ordinal()] = gateLit;
			dirty = true;
		}
	}

	public boolean isGateLit(EnumFacing direction) {
		return isGateLit[direction.ordinal()];
	}

	public void setIsGatePulsing(boolean gatePulsing, EnumFacing direction) {
		if (isGatePulsing[direction.ordinal()] != gatePulsing) {
			isGatePulsing[direction.ordinal()] = gatePulsing;
			dirty = true;
		}
	}

	public boolean isGatePulsing(EnumFacing direction) {
		return isGatePulsing[direction.ordinal()];
	}


	public void setIsGateExists(boolean gateExists, EnumFacing direction) {
		if (isGateExists[direction.ordinal()] != gateExists) {
			isGateExists[direction.ordinal()] = gateExists;
			dirty = true;
		}
	}

	public boolean isGateExists(EnumFacing direction) {
		return isGateExists[direction.ordinal()];
	}

	public void writeData(ByteBuf data) {
		for (int i = 0; i < EnumFacing.values().length; i++) {
			data.writeBoolean(isGateLit[i]);
			data.writeBoolean(isGatePulsing[i]);
			data.writeBoolean(isGateExists[i]);
			data.writeInt(gateIconIndex[i]);
		}
	}

	public void readData(ByteBuf data) {
		for (int i = 0; i < EnumFacing.values().length; i++) {
			isGateLit[i] = data.readBoolean();
			isGatePulsing[i] = data.readBoolean();
			isGateExists[i] = data.readBoolean();
			gateIconIndex[i] = data.readInt();
		}
	}
}
