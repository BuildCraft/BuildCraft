package buildcraft.transport;

import java.util.Collection;
import java.util.LinkedList;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IActionProvider;
import buildcraft.api.statements.IStatementContainer;

public class PipeActionProvider implements IActionProvider {

	@Override
	public Collection<IActionInternal> getInternalActions(IStatementContainer container) {
		LinkedList<IActionInternal> result = new LinkedList<IActionInternal>();
		Pipe<?> pipe = null;
		if (container instanceof IGate) {
			pipe = (Pipe<?>) ((IGate) container).getPipe();

			if (container instanceof Gate) {
				((Gate) container).addActions(result);
			}
		}

		if (pipe == null) {
			return result;
		}

		result.addAll(pipe.getActions());

		return result;
	}

	@Override
	public Collection<IActionExternal> getExternalActions(ForgeDirection side, TileEntity tile) {
		return null;
	}

}
