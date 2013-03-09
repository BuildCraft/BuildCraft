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
import buildcraft.transport.PipeTransportItems;

public class PipeItemsIron extends Pipe {

	private int standardIconIndex = IconTerrainConstants.PipeItemsIron_Standard;
	private int solidIconIndex = IconTerrainConstants.PipeAllIron_Solid;

	public PipeItemsIron(int itemID) {
		super(new PipeTransportItems(), new PipeLogicIron(), itemID);

		((PipeTransportItems) transport).allowBouncing = true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon[] getTextureIcons() {
		return BuildCraftTransport.instance.terrainIcons;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		if (direction == ForgeDirection.UNKNOWN)
			return standardIconIndex;
		else {
			int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

			if (metadata == direction.ordinal())
				return solidIconIndex;
			else
				return standardIconIndex;
		}
	}

	@Override
	public boolean canConnectRedstone() {
		return true;
	}
}
