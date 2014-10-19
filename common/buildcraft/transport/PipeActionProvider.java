package buildcraft.transport;

import java.util.Collection;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IActionProvider;
import buildcraft.api.transport.IPipeTile;

public class PipeActionProvider implements IActionProvider {

	@Override
	public Collection<IAction> getPipeActions(IPipeTile tile) {
		LinkedList<IAction> result = new LinkedList<IAction>();
		Pipe<?> pipe = null;
		if (tile instanceof TileGenericPipe) {
			pipe = ((TileGenericPipe) tile).pipe;
		}

		if (pipe == null) {
			return result;
		}
		
		result.addAll(pipe.getActions());

		for (Gate gate : pipe.gates) {
			if (gate != null) {
				gate.addActions(result);
			}
		}
		
		return result;
	}

	@Override
	public Collection<IAction> getNeighborActions(Block block, TileEntity tile) {
		return null;
	}

}
