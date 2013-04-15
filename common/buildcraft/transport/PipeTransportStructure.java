package buildcraft.transport;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class PipeTransportStructure extends PipeTransport {

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
