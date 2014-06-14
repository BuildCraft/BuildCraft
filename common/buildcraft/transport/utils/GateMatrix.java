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

public class GateMatrix {

	private final boolean[] isGateLit = new boolean[ForgeDirection.VALID_DIRECTIONS.length];
	private final boolean[] isGatePulsing = new boolean[ForgeDirection.VALID_DIRECTIONS.length];
	private final boolean[] isGateExists = new boolean[ForgeDirection.VALID_DIRECTIONS.length];
	private final int[] gateIconIndex = new int[ForgeDirection.VALID_DIRECTIONS.length];
	private boolean dirty = false;

	public GateMatrix() {
	}

	public boolean isDirty() {
		return dirty;
	}

	public void clean() {
		dirty = false;
	}

	public void setIsGateLit(boolean gateLit, ForgeDirection direction) {
		if (isGateLit[direction.ordinal()] != gateLit) {
			isGateLit[direction.ordinal()] = gateLit;
			dirty = true;
		}
	}

	public boolean isGateLit(ForgeDirection direction) {
		return isGateLit[direction.ordinal()];
	}

	public void setIsGatePulsing(boolean gatePulsing, ForgeDirection direction) {
		if (isGatePulsing[direction.ordinal()] != gatePulsing) {
			isGatePulsing[direction.ordinal()] = gatePulsing;
			dirty = true;
		}
	}

	public boolean isGatePulsing(ForgeDirection direction) {
		return isGatePulsing[direction.ordinal()];
	}


	public void setIsGateExists(boolean gateExists, ForgeDirection direction) {
		if (isGateExists[direction.ordinal()] != gateExists) {
			isGateExists[direction.ordinal()] = gateExists;
			dirty = true;
		}
	}

	public boolean isGateExists(ForgeDirection direction) {
		return isGateExists[direction.ordinal()];
	}

	public void writeData(ByteBuf data) {
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			data.writeBoolean(isGateLit[i]);
			data.writeBoolean(isGatePulsing[i]);
			data.writeBoolean(isGateExists[i]);
			data.writeInt(gateIconIndex[i]);
		}
	}

	public void readData(ByteBuf data) {
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			isGateLit[i] = data.readBoolean();
			isGatePulsing[i] = data.readBoolean();
			isGateExists[i] = data.readBoolean();
			gateIconIndex[i] = data.readInt();
		}
	}
}
