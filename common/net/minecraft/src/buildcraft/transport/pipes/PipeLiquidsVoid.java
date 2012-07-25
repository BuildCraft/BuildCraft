/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.transport.pipes;

import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.api.core.Orientations;
import net.minecraft.src.buildcraft.api.liquids.LiquidStack;
import net.minecraft.src.buildcraft.transport.IPipeTransportLiquidsHook;
import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.buildcraft.transport.PipeLogicVoid;
import net.minecraft.src.buildcraft.transport.PipeTransportLiquids;

public class PipeLiquidsVoid extends Pipe implements IPipeTransportLiquidsHook{

	public PipeLiquidsVoid(int itemID) {
		super(new PipeTransportLiquids(), new PipeLogicVoid(), itemID);
	}
	
	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}
	
	@Override
	public int getTextureIndex(Orientations direction) {
		return 9 * 16 + 14;
	}


	@Override
	public int fill(Orientations from, LiquidStack resource, boolean doFill) {
		return resource.amount;
	}
}
