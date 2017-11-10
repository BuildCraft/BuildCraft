/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.IBox;
import buildcraft.api.core.ISerializable;
import buildcraft.api.core.Position;
import buildcraft.core.lib.utils.LaserUtils;

public class Box implements IBox, ISerializable {
	public enum Kind {
		LASER_RED,
		LASER_YELLOW,
		LASER_GREEN,
		LASER_BLUE,
		STRIPES,
		BLUE_STRIPES,
	}

	public Kind kind = Kind.LASER_RED;
	public int xMin, yMin, zMin, xMax, yMax, zMax;
	public boolean initialized;
	public boolean isVisible = true;

	public LaserData[] lasersData;

	public Box() {
		reset();
	}

	public Box(TileEntity e) {
		initialize(e.xCoord, e.yCoord, e.zCoord, e.xCoord + 1, e.yCoord + 1,
				e.zCoord + 1);
	}

	public Box(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
		this();
		initialize(xMin, yMin, zMin, xMax, yMax, zMax);
	}

	public void reset() {
		initialized = false;
		xMin = Integer.MAX_VALUE;
		yMin = Integer.MAX_VALUE;
		zMin = Integer.MAX_VALUE;
		xMax = Integer.MAX_VALUE;
		yMax = Integer.MAX_VALUE;
		zMax = Integer.MAX_VALUE;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void initialize(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
		if (xMin < xMax) {
			this.xMin = xMin;
			this.xMax = xMax;
		} else {
			this.xMin = xMax;
			this.xMax = xMin;
		}

		if (yMin < yMax) {
			this.yMin = yMin;
			this.yMax = yMax;
		} else {
			this.yMin = yMax;
			this.yMax = yMin;
		}

		if (zMin < zMax) {
			this.zMin = zMin;
			this.zMax = zMax;
		} else {
			this.zMin = zMax;
			this.zMax = zMin;
		}

		initialized = !(xMin == Integer.MAX_VALUE || yMin == Integer.MAX_VALUE || zMin == Integer.MAX_VALUE || xMax == Integer.MAX_VALUE
				|| yMax == Integer.MAX_VALUE || zMax == Integer.MAX_VALUE);
	}

	public void initialize(Box box) {
		initialize(box.xMin, box.yMin, box.zMin, box.xMax, box.yMax, box.zMax);
	}

	public void initialize(IAreaProvider a) {
		initialize(a.xMin(), a.yMin(), a.zMin(), a.xMax(), a.yMax(), a.zMax());
	}

	public void initialize(NBTTagCompound nbttagcompound) {
		kind = Kind.values()[nbttagcompound.getShort("kind")];

		initialize(nbttagcompound.getInteger("xMin"),
				nbttagcompound.getInteger("yMin"),
				nbttagcompound.getInteger("zMin"),
				nbttagcompound.getInteger("xMax"),
				nbttagcompound.getInteger("yMax"),
				nbttagcompound.getInteger("zMax"));
	}

	public void initialize(int centerX, int centerY, int centerZ, int size) {
		initialize(centerX - size, centerY - size, centerZ - size, centerX + size, centerY + size, centerZ + size);
	}

	public List<BlockIndex> getBlocksInArea() {
		List<BlockIndex> blocks = new ArrayList<BlockIndex>();

		for (float x = xMin; x <= xMax; x++) {
			for (float y = yMin; y <= yMax; y++) {
				for (float z = zMin; z <= zMax; z++) {
					blocks.add(new BlockIndex((int) x, (int) y, (int) z));
				}
			}
		}

		return blocks;
	}

	@Override
	public Box expand(int amount) {
		xMin -= amount;
		yMin -= amount;
		zMin -= amount;
		xMax += amount;
		yMax += amount;
		zMax += amount;

		return this;
	}

	@Override
	public IBox contract(int amount) {
		return expand(-amount);
	}

	@Override
	public boolean contains(double x, double y, double z) {
		return contains(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z));
	}

	public boolean contains(int x, int y, int z) {
		return x >= xMin && x <= xMax && y >= yMin && y <= yMax && z >= zMin && z <= zMax;
	}

	public boolean contains(Position p) {
		return contains((int) p.x, (int) p.y, (int) p.z);
	}

	public boolean contains(BlockIndex i) {
		return contains(i.x, i.y, i.z);
	}

	@Override
	public Position pMin() {
		return new Position(xMin, yMin, zMin);
	}

	@Override
	public Position pMax() {
		return new Position(xMax, yMax, zMax);
	}

	public int sizeX() {
		return xMax - xMin + 1;
	}

	public int sizeY() {
		return yMax - yMin + 1;
	}

	public int sizeZ() {
		return zMax - zMin + 1;
	}

	public double centerX() {
		return xMin + sizeX() / 2.0;
	}

	public double centerY() {
		return yMin + sizeY() / 2.0;
	}

	public double centerZ() {
		return zMin + sizeZ() / 2.0;
	}

	public Box rotateLeft() {
		Box nBox = new Box();
		nBox.xMin = (sizeZ() - 1) - zMin;
		nBox.yMin = yMin;
		nBox.zMin = xMin;

		nBox.xMax = (sizeZ() - 1) - zMax;
		nBox.yMax = yMax;
		nBox.zMax = xMax;

		nBox.reorder();

		return nBox;
	}

	public void reorder() {
		int tmp;

		if (xMin > xMax) {
			tmp = xMin;
			xMin = xMax;
			xMax = tmp;
		}

		if (yMin > yMax) {
			tmp = yMin;
			yMin = yMax;
			yMax = tmp;
		}

		if (zMin > zMax) {
			tmp = zMin;
			zMin = zMax;
			zMax = tmp;
		}
	}

	@Override
	public void createLaserData() {
		lasersData = LaserUtils.createLaserDataBox(xMin, yMin, zMin, xMax, yMax, zMax);
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setByte("kind", (byte) kind.ordinal());

		nbttagcompound.setInteger("xMin", xMin);
		nbttagcompound.setInteger("yMin", yMin);
		nbttagcompound.setInteger("zMin", zMin);

		nbttagcompound.setInteger("xMax", xMax);
		nbttagcompound.setInteger("yMax", yMax);
		nbttagcompound.setInteger("zMax", zMax);
	}

	@Override
	public String toString() {
		return "{" + xMin + ", " + xMax + "}, {" + yMin + ", " + yMax + "}, {" + zMin + ", " + zMax + "}";
	}

	public Box extendToEncompass(Box toBeContained) {
		if (toBeContained == null || !toBeContained.initialized) {
			return this;
		}

		if (toBeContained.xMin < xMin) {
			xMin = toBeContained.xMin;
		}

		if (toBeContained.yMin < yMin) {
			yMin = toBeContained.yMin;
		}

		if (toBeContained.zMin < zMin) {
			zMin = toBeContained.zMin;
		}

		if (toBeContained.xMax > xMax) {
			xMax = toBeContained.xMax;
		}

		if (toBeContained.yMax > yMax) {
			yMax = toBeContained.yMax;
		}

		if (toBeContained.zMax > zMax) {
			zMax = toBeContained.zMax;
		}

		return this;
	}

	public AxisAlignedBB getBoundingBox() {
		return AxisAlignedBB.getBoundingBox(xMin, yMin, zMin,
				xMax, yMax, zMax);
	}

	public Box extendToEncompass(Position toBeContained) {
		if (toBeContained.x < xMin) {
			xMin = (int) toBeContained.x - 1;
		}

		if (toBeContained.y < yMin) {
			yMin = (int) toBeContained.y - 1;
		}

		if (toBeContained.z < zMin) {
			zMin = (int) toBeContained.z - 1;
		}

		if (toBeContained.x > xMax) {
			xMax = (int) toBeContained.x + 1;
		}

		if (toBeContained.y > yMax) {
			yMax = (int) toBeContained.y + 1;
		}

		if (toBeContained.z > zMax) {
			zMax = (int) toBeContained.z + 1;
		}

		return this;
	}

	public Box extendToEncompass(BlockIndex toBeContained) {
		if (toBeContained.x < xMin) {
			xMin = toBeContained.x - 1;
		}

		if (toBeContained.y < yMin) {
			yMin = toBeContained.y - 1;
		}

		if (toBeContained.z < zMin) {
			zMin = toBeContained.z - 1;
		}

		if (toBeContained.x > xMax) {
			xMax = toBeContained.x + 1;
		}

		if (toBeContained.y > yMax) {
			yMax = toBeContained.y + 1;
		}

		if (toBeContained.z > zMax) {
			zMax = toBeContained.z + 1;
		}

		return this;
	}

	@Override
	public double distanceTo(BlockIndex index) {
		return Math.sqrt(distanceToSquared(index));
	}

	@Override
	public double distanceToSquared(BlockIndex index) {
		int dx = index.x - (xMin + (xMax - xMin + 1));
		int dy = index.y - (yMin + (yMax - yMin + 1));
		int dz = index.z - (zMin + (zMax - zMin + 1));

		return dx * dx + dy * dy + dz * dz;
	}

	@Override
	public BlockIndex getRandomBlockIndex(Random rand) {
		int x = (xMax > xMin) ? xMin + rand.nextInt(xMax - xMin + 1) : xMin;
		int y = (yMax > yMin) ? yMin + rand.nextInt(yMax - yMin + 1) : yMin;
		int z = (zMax > zMin) ? zMin + rand.nextInt(zMax - zMin + 1) : zMin;

		return new BlockIndex(x, y, z);

	}

	@Override
	public void readData(ByteBuf stream) {
		byte flags = stream.readByte();
		xMin = stream.readInt();
		yMin = stream.readShort();
		zMin = stream.readInt();
		xMax = stream.readInt();
		yMax = stream.readShort();
		zMax = stream.readInt();

		kind = Kind.values()[flags & 31];
		initialized = (flags & 64) != 0;
		isVisible = (flags & 32) != 0;
	}

	@Override
	public void writeData(ByteBuf stream) {
		stream.writeByte((initialized ? 64 : 0) | (isVisible ? 32 : 0) | kind.ordinal());
		stream.writeInt(xMin);
		stream.writeShort(yMin);
		stream.writeInt(zMin);
		stream.writeInt(xMax);
		stream.writeShort(yMax);
		stream.writeInt(zMax);
	}

}
