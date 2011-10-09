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

import java.util.LinkedList;


public class API {

	public static LinkedList <LiquidData> liquids = new LinkedList <LiquidData> ();

	public static int getLiquidForBucket(int itemID) {
		for (LiquidData d : liquids) {
			if (d.filledBucketId == itemID) {
				return d.liquidId;
			}
		}
		
		return 0;
	}

	public static int getBucketForLiquid(int liquidId) {
		for (LiquidData d : liquids) {
			if (d.liquidId == liquidId) {
				return d.filledBucketId;
			}
		}
		
		return 0;
	}

}
