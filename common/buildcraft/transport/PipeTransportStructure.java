package buildcraft.transport;

import buildcraft.api.transport.IPipeTile.PipeType;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class PipeTransportStructure extends PipeTransport {

	@Override
	public PipeType getPipeType() {
		return PipeType.STRUCTURE;
	}

	@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		if (tile instanceof TileGenericPipe) {
			Pipe pipe2 = ((TileGenericPipe) tile).pipe;
			if (BlockGenericPipe.isValid(pipe2) && !(pipe2.transport instanceof PipeTransportStructure))
				return false;
		}

		return tile instanceof TileGenericPipe;
	}
}
