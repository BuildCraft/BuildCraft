/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.core.ISerializable;
import buildcraft.api.core.Position;

public class LaserData implements ISerializable {
	public Position head = new Position(0, 0, 0);
	public Position tail = new Position(0, 0, 0);
	public boolean isVisible = true;
	public boolean isGlowing = false;

	public double renderSize = 1.0 / 16.0;
	public double angleY = 0;
	public double angleZ = 0;

	public double wavePosition = 0;
	public int laserTexAnimation = 0;

	// Size of the wave, from 0 to 1
	public float waveSize = 1F;

	public LaserData() {

	}

	public LaserData(Position tail, Position head) {
		this.tail.x = tail.x;
		this.tail.y = tail.y;
		this.tail.z = tail.z;

		this.head.x = head.x;
		this.head.y = head.y;
		this.head.z = head.z;
	}

	public void update() {
		double dx = head.x - tail.x;
		double dy = head.y - tail.y;
		double dz = head.z - tail.z;

		renderSize = Math.sqrt(dx * dx + dy * dy + dz * dz);
		angleZ = 360 - (Math.atan2(dz, dx) * 180.0 / Math.PI + 180.0);
		dx = Math.sqrt(renderSize * renderSize - dy * dy);
		angleY = -Math.atan2(dy, dx) * 180.0 / Math.PI;
	}

	public void iterateTexture() {
		laserTexAnimation = (laserTexAnimation + 1) % 40;
	}

	public void writeToNBT(NBTTagCompound nbt) {
		NBTTagCompound headNbt = new NBTTagCompound();
		head.writeToNBT(headNbt);
		nbt.setTag("head", headNbt);

		NBTTagCompound tailNbt = new NBTTagCompound();
		tail.writeToNBT(tailNbt);
		nbt.setTag("tail", tailNbt);

		nbt.setBoolean("isVisible", isVisible);
	}

	public void readFromNBT(NBTTagCompound nbt) {
		head.readFromNBT(nbt.getCompoundTag("head"));
		tail.readFromNBT(nbt.getCompoundTag("tail"));
		isVisible = nbt.getBoolean("isVisible");
	}

	@Override
	public void readData(ByteBuf stream) {
		head.readData(stream);
		tail.readData(stream);
		int flags = stream.readUnsignedByte();
		isVisible = (flags & 1) != 0;
		isGlowing = (flags & 2) != 0;
	}

	@Override
	public void writeData(ByteBuf stream) {
		head.writeData(stream);
		tail.writeData(stream);
		int flags = (isVisible ? 1 : 0) | (isGlowing ? 2 : 0);
		stream.writeByte(flags);
	}
}
