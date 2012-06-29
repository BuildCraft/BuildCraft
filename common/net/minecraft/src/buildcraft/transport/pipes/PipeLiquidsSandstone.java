/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.transport.pipes;

import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.transport.IPipeTransportLiquidsHook;
import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.buildcraft.transport.PipeLogicSandstone;
import net.minecraft.src.buildcraft.transport.PipeTransportLiquids;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;

public class PipeLiquidsSandstone extends Pipe implements IPipeTransportLiquidsHook{
	 public PipeLiquidsSandstone(int itemID) {
			super(new PipeTransportLiquids(), new PipeLogicSandstone(), itemID);
	}
	 
	 @Override
	public int getMainBlockTexture() {
		 return 9 * 16 + 15;
	}

	@Override
	public int fill(Orientations from, int quantity, int id, boolean doFill) {
		if (container.tileBuffer == null || container.tileBuffer[from.ordinal()] == null)
			return 0;
		
		if (!(container.tileBuffer[from.ordinal()].getTile() instanceof TileGenericPipe)) 
			return 0;
		
		return ((PipeTransportLiquids)this.transport).side[from.ordinal()].fill(quantity, doFill, (short) id);
	}
}