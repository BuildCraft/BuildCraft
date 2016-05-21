package buildcraft.transport;

import java.util.Collection;
import java.util.LinkedList;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IActionProvider;
import buildcraft.api.statements.IStatementContainer;

public class PipeActionProvider implements IActionProvider {

    @Override
    public void addInternalActions(Collection<IActionInternal> actions, IStatementContainer container) {
        Pipe<?> pipe = null;
        if (container instanceof IGate) {
            pipe = (Pipe<?>) ((IGate) container).getPipe();
        }

        if (pipe != null) {
            actions.addAll(pipe.getActions());
        }

        if (container instanceof Gate) {
            ((Gate) container).addActions(actions);
        }
    }

    @Override
    public void addExternalActions(Collection<IActionExternal> actions, EnumFacing side, TileEntity tile) {

    }
}
