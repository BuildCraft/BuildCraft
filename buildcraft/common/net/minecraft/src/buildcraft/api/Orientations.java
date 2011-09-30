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
