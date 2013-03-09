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
import buildcraft.BuildCraftTransport;
import buildcraft.core.DefaultProps;
import buildcraft.transport.IconTerrainConstants;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;

public class PipeItemsSandstone extends Pipe {
	public PipeItemsSandstone(int itemID) {
		super(new PipeTransportItems(), new PipeLogicSandstone(), itemID);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon[] getTextureIcons() {
		return BuildCraftTransport.instance.terrainIcons;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return IconTerrainConstants.PipeItemsSandstone;
	}
}
