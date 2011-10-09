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

public class LiquidData {

	public final int liquidId;
	public final int filledBucketId;
	
	public LiquidData (int liquidId, int filledBucketId) {
		this.liquidId = liquidId;
		this.filledBucketId = filledBucketId;
	}
	
}
