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
import buildcraft.transport.IconTerrainConstants;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportLiquids;

public class PipeLiquidsVoid extends Pipe implements IPipeTransportLiquidsHook {

	public PipeLiquidsVoid(int itemID) {
		super(new PipeTransportLiquids(), new PipeLogicVoid(), itemID);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon[] getTextureIcons() {
		return BuildCraftTransport.instance.terrainIcons;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return IconTerrainConstants.PipeLiquidsVoid;
	}

	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill) {
		return resource.amount;
	}
}
