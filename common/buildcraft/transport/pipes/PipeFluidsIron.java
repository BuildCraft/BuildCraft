/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile or
 * run the code. It does *NOT* grant the right to redistribute this software or
 * its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */
package buildcraft.transport.pipes;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.IPipeEntry;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;

public class PipeFluidsIron extends Pipe {

	protected int standardIconIndex = PipeIconProvider.TYPE.PipeFluidsIron_Standard.ordinal();
	protected int solidIconIndex = PipeIconProvider.TYPE.PipeAllIron_Solid.ordinal();

	public PipeFluidsIron(int itemID) {
		super(new PipeTransportFluids(), new PipeLogicIron(), itemID);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		if (direction == ForgeDirection.UNKNOWN)
			return standardIconIndex;
		else {
			int metadata = container.getBlockMetadata();

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
