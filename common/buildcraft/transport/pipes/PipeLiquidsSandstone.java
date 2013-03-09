/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport.pipes;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.LiquidStack;
import buildcraft.BuildCraftTransport;
import buildcraft.core.DefaultProps;
import buildcraft.transport.IPipeTransportLiquidsHook;
import buildcraft.transport.IconConstants;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportLiquids;
import buildcraft.transport.TileGenericPipe;

public class PipeLiquidsSandstone extends Pipe implements IPipeTransportLiquidsHook {
	public PipeLiquidsSandstone(int itemID) {
		super(new PipeTransportLiquids(), new PipeLogicSandstone(), itemID);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon[] getTextureIcons() {
		return BuildCraftTransport.instance.icons;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return IconConstants.PipeLiquidsSandstone;
	}

	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill) {
		if (container.tileBuffer == null || container.tileBuffer[from.ordinal()] == null)
			return 0;

		if (!(container.tileBuffer[from.ordinal()].getTile() instanceof TileGenericPipe))
			return 0;

		return ((PipeTransportLiquids) this.transport).getTanks(ForgeDirection.UNKNOWN)[from.ordinal()].fill(resource, doFill);
	}
}
