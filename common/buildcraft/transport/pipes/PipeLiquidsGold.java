/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
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
import buildcraft.transport.PipeTransportLiquids;

public class PipeLiquidsGold extends Pipe {

	public PipeLiquidsGold(int itemID) {
		super(new PipeTransportLiquids(), new PipeLogicGold(), itemID);

		((PipeTransportLiquids) transport).flowRate = 40;
		((PipeTransportLiquids) transport).travelDelay = 4;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon[] getTextureIcons() {
		return BuildCraftTransport.instance.terrainIcons;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return IconTerrainConstants.PipeLiquidsGold;
	}

}
