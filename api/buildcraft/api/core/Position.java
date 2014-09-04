/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.core;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class Position {

	@NetworkData
	public double x, y, z;

	@NetworkData
	public ForgeDirection orientation;

	public Position() {
		x = 0;
		y = 0;
		z = 0;
		orientation = ForgeDirection.UNKNOWN;
	}

	public Position(double ci, double cj, double ck) {
		x = ci;
		y = cj;
		z = ck;
		orientation = ForgeDirection.UNKNOWN;
	}

	public Position(double ci, double cj, double ck, ForgeDirection corientation) {
		x = ci;
		y = cj;
		z = ck;
		orientation = corientation;

		if (orientation == null) {
			orientation = ForgeDirection.UNKNOWN;
		}
	}

	public Position(Position p) {
		x = p.x;
		y = p.y;
		z = p.z;
		orientation = p.orientation;
	}

	public Position(NBTTagCompound nbttagcompound) {
		readFromNBT(nbttagcompound);
	}

	public Position(TileEntity tile) {
		x = tile.xCoord;
		y = tile.yCoord;
		z = tile.zCoord;
		orientation = ForgeDirection.UNKNOWN;
	}

	public void moveRight(double step) {
		switch (orientation) {
		case SOUTH:
			x = x - step;
			break;
		case NORTH:
			x = x + step;
			break;
		case EAST:
			z = z + step;
			break;
		case WEST:
			z = z - step;
			break;
		default:
		}
	}

	public void moveLeft(double step) {
		moveRight(-step);
	}

	public void moveForwards(double step) {
		switch (orientation) {
		case UP:
			y = y + step;
			break;
		case DOWN:
			y = y - step;
			break;
		case SOUTH:
			z = z + step;
			break;
		case NORTH:
			z = z - step;
			break;
		case EAST:
			x = x + step;
			break;
		case WEST:
			x = x - step;
			break;
		default:
		}
	}

	public void moveBackwards(double step) {
		moveForwards(-step);
	}

	public void moveUp(double step) {
		switch (orientation) {
		case SOUTH:
		case NORTH:
		case EAST:
		case WEST:
			y = y + step;
			break;
		default:
		}

	}

	public void moveDown(double step) {
		moveUp(-step);
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		if (orientation == null) {
			orientation = ForgeDirection.UNKNOWN;
		}

		nbttagcompound.setDouble("i", x);
		nbttagcompound.setDouble("j", y);
		nbttagcompound.setDouble("k", z);
		nbttagcompound.setByte("orientation", (byte) orientation.ordinal());
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		x = nbttagcompound.getDouble("i");
		y = nbttagcompound.getDouble("j");
		z = nbttagcompound.getDouble("k");
		orientation = ForgeDirection.values() [nbttagcompound.getByte("orientation")];
	}

	public Position shift(int dir) {
		return shift(dir, 1.0);
	}

	public Position shift(ForgeDirection dir) {
		return shift(dir, 1.0);
	}

	public Position shift(int dir, double steps) {
		return shift(ForgeDirection.getOrientation(dir), steps);
	}

	public Position shift(ForgeDirection dir, double steps) {
		return offset(dir.offsetX * steps, dir.offsetY * steps, dir.offsetZ * steps);
	}

	public Position offset(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public Position offset(Position pos) {
		return offset(pos.x, pos.y, pos.z);
	}

	public Block getBlock(IBlockAccess iba) {
		return iba.getBlock((int) x, (int) y, (int) z);
	}

	public int getMeta(IBlockAccess iba) {
		return iba.getBlockMetadata((int) x, (int) y, (int) z);
	}

	public TileEntity getTile(IBlockAccess iba) {
		return iba.getTileEntity((int) x, (int) y, (int) z);
	}

	public boolean blockExists(IBlockAccess wrd) {
		return !getBlock(wrd).isAir(wrd, (int) x, (int) y, (int) z);
	}

	public Position setBlock(World wrd, Block block) {
		wrd.setBlock((int) x, (int) y, (int) z, block);
		return this;
	}

	public Position setBlock(World wrd, Block block, int meta) {
		wrd.setBlock((int) x, (int) y, (int) z, block, meta, 3);
		return this;
	}

	public Position setMeta(World wrd, int meta) {
		wrd.setBlockMetadataWithNotify((int) x, (int) y, (int) z, meta, 3);
		return this;
	}

	public boolean destroyBlock(World wrd, boolean doDrop) {
		return wrd.func_147480_a((int) x, (int) y, (int) z, doDrop);
	}

	@Override
	public String toString() {
		return "{" + x + ", " + y + ", " + z + "}";
	}

	public Position copy() {
		return new Position(x, y, z, orientation);
	}

	public Position min(Position p) {
		return new Position(p.x > x ? x : p.x, p.y > y ? y : p.y, p.z > z ? z : p.z);
	}

	public Position max(Position p) {
		return new Position(p.x < x ? x : p.x, p.y < y ? y : p.y, p.z < z ? z : p.z);
	}

	public boolean isClose(Position newPosition, float f) {
		double dx = x - newPosition.x;
		double dy = y - newPosition.y;
		double dz = z - newPosition.z;

		double sqrDis = dx * dx + dy * dy + dz * dz;

		return !(sqrDis > f * f);
	}
}
