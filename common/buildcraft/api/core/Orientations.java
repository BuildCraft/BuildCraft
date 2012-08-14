/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.api.core;

import net.minecraftforge.common.Orientation;

public enum Orientations {
	YNeg, // 0
	YPos, // 1
	ZNeg, // 2
	ZPos, // 3
	XNeg, // 4
	XPos, // 5
	Unknown;

	public Orientations reverse() {
		switch (this) {
		case YPos:
			return Orientations.YNeg;
		case YNeg:
			return Orientations.YPos;
		case ZPos:
			return Orientations.ZNeg;
		case ZNeg:
			return Orientations.ZPos;
		case XPos:
			return Orientations.XNeg;
		case XNeg:
			return Orientations.XPos;
		default:
			return Orientations.Unknown;
		}
	}
	
	public Orientation toOrientation(){
		switch(this){
		case YNeg:
			return Orientation.DOWN;
		case YPos:
			return Orientation.UP;
		case ZNeg:
			return Orientation.NORTH;
		case ZPos:
			return Orientation.SOUTH;
		case XNeg:
			return Orientation.WEST;
		case XPos:
			return Orientation.EAST;
		default:
			return Orientation.UNKNOWN;
			
		}
	}

	public Orientations rotateLeft() {
		switch (this) {
		case XPos:
			return ZPos;
		case ZNeg:
			return XPos;
		case XNeg:
			return ZNeg;
		case ZPos:
			return XNeg;
		default:
			return this;
		}
	}

	public static Orientations[] dirs() {
		return new Orientations[] { YNeg, YPos, ZNeg, ZPos, XNeg, XPos };
	}
}
