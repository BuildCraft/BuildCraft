package buildcraft.builders.triggers;

import buildcraft.api.filler.FillerManager;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IActionProvider;
import buildcraft.builders.TileFiller;
import java.util.LinkedList;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class BuildersActionProvider implements IActionProvider {

	@Override
	public LinkedList<IAction> getNeighborActions(Block block, TileEntity tile) {
		LinkedList<IAction> actions = new LinkedList<IAction>();
		if (tile instanceof TileFiller) {
			actions.addAll(FillerManager.registry.getActions());
		}
		return actions;
	}
}
