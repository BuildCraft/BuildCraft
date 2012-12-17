package buildcraft.transport.pipes;

import net.minecraftforge.common.ForgeDirection;
import buildcraft.core.DefaultProps;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportStructure;

public class PipeStructureCobblestone extends Pipe {

	public PipeStructureCobblestone(int itemID) {
		super(new PipeTransportStructure(), new PipeLogicCobblestone(), itemID);

	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

	@Override
	public int getTextureIndex(ForgeDirection direction) {
		return 7 * 16 + 13;
	}

}
