/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import io.netty.buffer.ByteBuf;

import buildcraft.api.core.ISerializable;
import buildcraft.transport.utils.ConnectionMatrix;
import buildcraft.transport.utils.TextureMatrix;
import buildcraft.transport.utils.WireMatrix;

public class PipeRenderState implements ISerializable {
	public final ConnectionMatrix pipeConnectionMatrix = new ConnectionMatrix();
	public final TextureMatrix textureMatrix = new TextureMatrix();
	public final WireMatrix wireMatrix = new WireMatrix();
	private boolean glassColorDirty = false;
	private byte glassColor = -127;

	private boolean dirty = true;

	public void clean() {
		dirty = false;
		glassColorDirty = false;
		pipeConnectionMatrix.clean();
		textureMatrix.clean();
		wireMatrix.clean();
	}

	public byte getGlassColor() {
		return glassColor;
	}

	public void setGlassColor(byte color) {
		if (this.glassColor != color) {
			this.glassColor = color;
			this.glassColorDirty = true;
		}
	}

	public boolean isDirty() {
		return dirty || pipeConnectionMatrix.isDirty() || glassColorDirty
				|| textureMatrix.isDirty() || wireMatrix.isDirty();
	}

	public boolean needsRenderUpdate() {
		return glassColorDirty || pipeConnectionMatrix.isDirty() || textureMatrix.isDirty();
	}

	@Override
	public void writeData(ByteBuf data) {
		data.writeByte(glassColor < -1 ? -1 : glassColor);
		pipeConnectionMatrix.writeData(data);
		textureMatrix.writeData(data);
		wireMatrix.writeData(data);
	}

	@Override
	public void readData(ByteBuf data) {
		byte g = data.readByte();
		if (g != glassColor) {
			this.glassColor = g;
			this.glassColorDirty = true;
		}
		pipeConnectionMatrix.readData(data);
		textureMatrix.readData(data);
		wireMatrix.readData(data);
	}
}
