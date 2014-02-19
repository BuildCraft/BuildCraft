/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.IBox;
import buildcraft.api.core.LaserKind;
import buildcraft.api.core.Position;
import buildcraft.core.network.NetworkData;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;

public class Box implements IBox {

	public @NetworkData
	float xMin, yMin, zMin, xMax, yMax, zMax;
	public @NetworkData
	boolean initialized;

	// TODO: we should convert all boxes to the interface that draws it directly
	// rather than the one that relies on additional entities. Amongst other
	// things, this is a substancial save of synchronization data, as only
	// boundaries need to be carried over.
	private EntityBlock lasers[];

	public LaserData lasersData [];

	public Box() {
		reset();
	}

	public Box(float xMin, float yMin, float zMin, float xMax, float yMax, float zMax) {
		this ();
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

	public void initialize(float xMin, float yMin, float zMin, float xMax, float yMax, float zMax) {
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
		initialize(nbttagcompound.getFloat("xMin"),
				nbttagcompound.getFloat("yMin"),
				nbttagcompound.getFloat("zMin"),
				nbttagcompound.getFloat("xMax"),
				nbttagcompound.getFloat("yMax"),
				nbttagcompound.getFloat("zMax"));
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
	public void expand(int amount) {
		xMin += amount;
		yMin += amount;
		zMin += amount;
		xMax += amount;
		yMax += amount;
		zMax += amount;
	}

	@Override
	public void contract(int amount) {
		expand(-amount);
	}

	@Override
	public boolean contains(int x, int y, int z) {
		if (x >= xMin && x <= xMax && y >= yMin && y <= yMax && z >= zMin && z <= zMax) {
			return true;
		} else {
			return false;
		}
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

	public float sizeX() {
		return xMax - xMin + 1;
	}

	public float sizeY() {
		return yMax - yMin + 1;
	}

	public float sizeZ() {
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
		float tmp;

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
	public void createLasers(World world, LaserKind kind) {
		if (lasers == null) {
			lasers = Utils.createLaserBox(world, xMin, yMin, zMin, xMax, yMax, zMax, kind);
		}
	}

	public void createLaserData() {
		lasersData = Utils.createLaserDataBox(xMin, yMin, zMin, xMax, yMax, zMax);
	}

	@Override
	public void deleteLasers() {
		if (lasers != null) {
			for (EntityBlock b : lasers) {
				CoreProxy.proxy.removeEntity(b);
			}

			lasers = null;
		}
	}

	public void writeToStream(ByteBuf stream) {
		stream.writeBoolean(initialized);

		stream.writeFloat(xMin);
		stream.writeFloat(yMin);
		stream.writeFloat(zMin);

		stream.writeFloat(xMax);
		stream.writeFloat(yMax);
		stream.writeFloat(zMax);
	}

	public void readFromStream(ByteBuf stream) {
		initialized = stream.readBoolean();

		xMin = stream.readFloat();
		yMin = stream.readFloat();
		zMin = stream.readFloat();

		xMax = stream.readFloat();
		yMax = stream.readFloat();
		zMax = stream.readFloat();
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setFloat("xMin", xMin);
		nbttagcompound.setFloat("yMin", yMin);
		nbttagcompound.setFloat("zMin", zMin);

		nbttagcompound.setFloat("xMax", xMax);
		nbttagcompound.setFloat("yMax", yMax);
		nbttagcompound.setFloat("zMax", zMax);
	}

	@Override
	public String toString() {
		return "{" + xMin + ", " + xMax + "}, {" + yMin + ", " + yMax + "}, {" + zMin + ", " + zMax + "}";
	}
}
