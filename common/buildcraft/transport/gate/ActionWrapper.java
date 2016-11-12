package buildcraft.transport.gate;

import net.minecraft.tileentity.TileEntity;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.statements.*;

public abstract class ActionWrapper extends StatementWrapper implements IActionInternal {

    protected boolean isActive = false;

    public ActionWrapper(IStatement delegate, EnumPipePart sourcePart) {
        super(delegate, sourcePart);
    }

    public void actionDeactivated(IStatementContainer source, IStatementParameter[] parameters) {
        isActive = false;
    }

    public static class ActionWrapperInternal extends ActionWrapper {
        public final IActionInternal action;

        public ActionWrapperInternal(IActionInternal action) {
            super(action, EnumPipePart.CENTER);
            this.action = action;
        }

        @Override
        public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {
            if (isActive && action instanceof IActionSingle) {
                if (((IActionSingle) action).singleActionTick()) {
                    return;
                }
            }
            this.action.actionActivate(source, parameters);
            isActive = true;
        }
    }

    public static class ActionWrapperInternalSided extends ActionWrapper {
        public final IActionInternalSided action;

        public ActionWrapperInternalSided(IActionInternalSided action, EnumPipePart sourcePart) {
            super(action, sourcePart);
            this.action = action;
        }

        @Override
        public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {
            if (isActive && action instanceof IActionSingle) {
                if (((IActionSingle) action).singleActionTick()) {
                    return;
                }
            }
            action.actionActivate(sourcePart.face, source, parameters);
            isActive = true;
        }
    }

    public static class ActionWrapperExternal extends ActionWrapper {
        public final IActionExternal action;

        public ActionWrapperExternal(IActionExternal action, EnumPipePart sourcePart) {
            super(action, sourcePart);
            this.action = action;
        }

        @Override
        public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {
            if (isActive && action instanceof IActionSingle) {
                if (((IActionSingle) action).singleActionTick()) {
                    return;
                }
            }
            TileEntity neighbourTile = getNeighbourTile(source);
            action.actionActivate(neighbourTile, sourcePart.face, source, parameters);
            if (neighbourTile instanceof IActionReceptor) {
                IActionReceptor receptor = (IActionReceptor) neighbourTile;
                receptor.actionActivated(action, parameters);
            }
            isActive = true;
        }
    }
}
