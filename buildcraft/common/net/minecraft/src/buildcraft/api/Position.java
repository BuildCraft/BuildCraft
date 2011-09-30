/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 * 
 * As a special exception, this file is part of the BuildCraft API and is 
 * allowed to be redistributed, either in source or binaries form.
 */

package net.minecraft.src.buildcraft.api;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

public class Position {
	public double x, y, z;
	public Orientations orientation;
	
	public Position (double ci, double cj, double ck) {
		x = ci;
		y = cj;
		z = ck;
		orientation = Orientations.Unknown;
	}
	
	public Position (double ci, double cj, double ck, Orientations corientation) {
		x = ci;
		y = cj;
		z = ck;
		orientation = corientation;
	}
	
	public Position (Position p) {
		x = p.x;
		y = p.y;
		z = p.z;
		orientation = p.orientation;
	}
	
	public Position (NBTTagCompound nbttagcompound) {
		x = nbttagcompound.getDouble("i");
		y = nbttagcompound.getDouble("j");
		z = nbttagcompound.getDouble("k");
		
		orientation = Orientations.Unknown;
	}
	
	public Position (TileEntity tile) {
		x = tile.xCoord;
		y = tile.yCoord;
		z = tile.zCoord;
	}
	
	public void moveRight (double step) {
		switch (orientation) {
		case ZPos:
			x = x - step;
			break;
		case ZNeg:
			x = x + step;    			
			break;
		case XPos:
			z = z + step;
			break;
		case XNeg:
			z = z - step;
			break;
		}
	}
	
	public void moveLeft (double step) {
		moveRight(-step);
	}
	
	public void moveForwards (double step) {
		switch (orientation) {
		case YPos:
			y = y + step;
			break;
		case YNeg:
			y = y - step;
			break;
		case ZPos:
			z = z + step;
			break;
		case ZNeg:
			z = z - step;	
			break;
		case XPos:
			x = x + step;
			break;		
		case XNeg:
			x = x - step;
			break;
		}
	}	
	
	public void moveBackwards (double step) {
		moveForwards(-step);
	}
	
	public void moveUp (double step) {
		switch (orientation) {
		case ZPos: case ZNeg: case XPos: case XNeg:
			y = y + step;
			break;
		}
		
	}
	
	public void moveDown (double step) {
		moveUp (-step);
	}
	
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setDouble("i", x);
		nbttagcompound.setDouble("j", y);
		nbttagcompound.setDouble("k", z);
	}
	
	public String toString () {
		return "{" + x + ", " + y + ", " + z + "}";
	}
	
	public Position min (Position p) {
		return new Position(p.x > x ? x : p.x, p.y > y ? y : p.y, p.z > z ? z
				: p.z);
	}
	
	public Position max (Position p) {
		return new Position(p.x < x ? x : p.x, p.y < y ? y : p.y, p.z < z ? z
				: p.z);
	}
	
}