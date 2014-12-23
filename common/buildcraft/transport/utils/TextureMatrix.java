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

public class TextureMatrix {

	private final int[] iconIndexes = new int[7];
	private boolean dirty = false;

	public int getTextureIndex(EnumFacing direction) {
		int v = direction != null ? direction.ordinal() : 6;
		return iconIndexes[v];
	}

	public void setIconIndex(EnumFacing direction, int value) {
		int v = direction != null ? direction.ordinal() : 6;
		if (iconIndexes[v] != value) {
			iconIndexes[v] = value;
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
		for (int iconIndexe : iconIndexes) {
			data.writeByte(iconIndexe);
		}
	}

	public void readData(ByteBuf data) {
		for (int i = 0; i < iconIndexes.length; i++) {
			int icon = data.readByte();
			if (iconIndexes[i] != icon) {
				iconIndexes[i] = icon;
				dirty = true;
			}
		}
	}
}
