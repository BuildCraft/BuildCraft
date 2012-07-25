/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.transport.pipes;

import net.minecraft.src.buildcraft.api.core.Orientations;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.buildcraft.transport.PipeLogicSandstone;
import net.minecraft.src.buildcraft.transport.PipeTransportItems;

public class PipeItemsSandstone extends Pipe{
	public PipeItemsSandstone(int itemID) {
		super(new PipeTransportItems(), new PipeLogicSandstone(), itemID);
	}
	 
	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}
	
	@Override
	public int getTextureIndex(Orientations direction) {
		return 8 * 16 + 15;
	}
}