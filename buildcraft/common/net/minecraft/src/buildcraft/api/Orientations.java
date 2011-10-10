/** 
 * Copyright (c) SpaceToad, 2011
 * 
 * This file is part of the BuildCraft API. You have the rights to read, 
 * modify, compile or run this the code without restrictions. In addition, it
 * allowed to redistribute this API as well, either in source or binaries 
 * form, or to integrate it into an other mod.
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
