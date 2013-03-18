package buildcraft.transport.pipes;

import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportStructure;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipeStructureCobblestone extends Pipe {

	public PipeStructureCobblestone(int itemID) {
		super(new PipeTransportStructure(), new PipeLogicCobblestone(), itemID);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}
	
	@Override
	public int getIconIndex(ForgeDirection direction) {
		return PipeIconProvider.PipeStructureCobblestone;
	}
}
