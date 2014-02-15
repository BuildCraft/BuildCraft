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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraftforge.common.util.ForgeDirection;

public class TextureMatrix {

	private final int[] _iconIndexes = new int[7];
	private boolean dirty = false;

	public int getTextureIndex(ForgeDirection direction) {
		return _iconIndexes[direction.ordinal()];
	}

	public void setIconIndex(ForgeDirection direction, int value) {
		if (_iconIndexes[direction.ordinal()] != value) {
			_iconIndexes[direction.ordinal()] = value;
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
		for (int i = 0; i < _iconIndexes.length; i++) {
			data.writeByte(_iconIndexes[i]);
		}
	}

	public void readData(ByteBuf data) {
		for (int i = 0; i < _iconIndexes.length; i++) {
			int icon = data.readByte();
			if (_iconIndexes[i] != icon) {
				_iconIndexes[i] = icon;
				dirty = true;
			}
		}
	}
}
