/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.core;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.IAreaProvider;
import net.minecraft.src.buildcraft.api.IBox;
import net.minecraft.src.buildcraft.api.LaserKind;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.api.TileNetworkData;

public class Box implements IBox {

	public @TileNetworkData int xMin, yMin, zMin, xMax, yMax, zMax;

	private EntityBlock lasers [];

	public void initialize (Box box) {
		this.xMin = box.xMin;
		this.yMin = box.yMin;
		this.zMin = box.zMin;
		this.xMax = box.xMax;
		this.yMax = box.yMax;
		this.zMax = box.zMax;
	}

	public void initialize (int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
		this.xMin = xMin;
		this.yMin = yMin;
		this.zMin = zMin;
		this.xMax = xMax;
		this.yMax = yMax;
		this.zMax = zMax;
	}

	public void initialize (IAreaProvider area) {
		xMin = area.xMin();
		yMin = area.yMin();
		zMin = area.zMin();
		xMax = area.xMax();
		yMax = area.yMax();
		zMax = area.zMax();
	}

	public void initialize (NBTTagCompound nbttagcompound) {
		xMin = nbttagcompound.getInteger("xMin");
		yMin = nbttagcompound.getInteger("yMin");
		zMin = nbttagcompound.getInteger("zMin");
		xMax = nbttagcompound.getInteger("xMax");
		yMax = nbttagcompound.getInteger("yMax");
		zMax = nbttagcompound.getInteger("zMax");
	}

	public Box() {
		reset ();
	}

	public boolean isInitialized () {
		return xMin != Integer.MAX_VALUE;
	}

	public void reset () {
		xMin = Integer.MAX_VALUE;
		yMin = Integer.MAX_VALUE;
		zMin = Integer.MAX_VALUE;
		xMax = Integer.MAX_VALUE;
		yMax = Integer.MAX_VALUE;
		zMax = Integer.MAX_VALUE;
	}

	public static int packetSize () {
		return 6;
	}

	@Override
	public Position pMin () {
		return new Position (xMin, yMin, zMin);
	}

	@Override
	public Position pMax () {
		return new Position (xMax, yMax, zMax);
	}

	@Override
	public void createLasers (World world, LaserKind kind) {
		if (lasers == null)
			lasers = Utils.createLaserBox(world, xMin, yMin, zMin, xMax, yMax,
					zMax, kind);
	}

	@Override
	public void deleteLasers () {
		if (lasers != null) {
			for (EntityBlock b : lasers)
				APIProxy.removeEntity(b);

			lasers = null;
		}
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("xMin", xMin);
		nbttagcompound.setInteger("yMin", yMin);
		nbttagcompound.setInteger("zMin", zMin);

		nbttagcompound.setInteger("xMax", xMax);
		nbttagcompound.setInteger("yMax", yMax);
		nbttagcompound.setInteger("zMax", zMax);

	}

	public int sizeX () {
		return xMax - xMin + 1;
	}

	public int sizeY () {
		return yMax - yMin + 1;
	}

	public int sizeZ () {
		return zMax - zMin + 1;
	}

	@Override
	public String toString () {
		return "{" + xMin + ", " + xMax + "}, {" + yMin + ", " + yMax + "}, {"
		+ zMin + ", " + zMax + "}";
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

	public Box rotateLeft () {
		Box nBox = new Box ();
		nBox.xMin = (sizeZ() - 1) - zMin;
		nBox.yMin = yMin;
		nBox.zMin = xMin;

		nBox.xMax = (sizeZ() - 1) - zMax;
		nBox.yMax = yMax;
		nBox.zMax = xMax;

		nBox.reorder ();

		return nBox;
	}

	public void reorder () {
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
}
