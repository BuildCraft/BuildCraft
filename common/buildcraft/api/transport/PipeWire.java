/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.transport;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public enum PipeWire {
	Red, Blue, Green, Yellow;

	public PipeWire reverse() {
		switch (this) {
			case Red:
				return Yellow;
			case Blue:
				return Green;
			case Green:
				return Blue;
			default:
				return Red;
		}
	}
    
}
