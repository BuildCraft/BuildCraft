/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.api;

public enum Orientations {
	YNeg, // 0
	YPos, // 1
	ZNeg, // 2
	ZPos, // 3
	XNeg, // 4
	XPos, // 5
	Unknown;
	
	public Orientations reverse () {
		switch (this) {
		case YPos:
			return  Orientations.YNeg;
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
		}
		
		return Orientations.Unknown;
	}
}
