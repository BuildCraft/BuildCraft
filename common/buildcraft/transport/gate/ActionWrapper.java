/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.gate;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.statements.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

public abstract class ActionWrapper extends StatementWrapper implements IActionInternal {

    protected boolean isActive = false;

    public ActionWrapper(IAction delegate, EnumPipePart sourcePart) {
        super(delegate, sourcePart);
    }

    public IAction getDelegate() {
        return (IAction) delegate;
    }

    public static ActionWrapper wrap(IStatement statement, EnumFacing side) {
        if (statement == null) {
            return null;
        } else if (statement instanceof ActionWrapper) {
            return (ActionWrapper) statement;
        } else if (statement instanceof IActionInternal && side == null) {
            return new ActionWrapperInternal((IActionInternal) statement);
        } else if (statement instanceof IActionInternalSided) {
            if (side == null) {
                throw new NullPointerException("side");
            }
            return new ActionWrapperInternalSided((IActionInternalSided) statement, side);
        } else if (statement instanceof IActionExternal) {
            if (side == null) {
                throw new NullPointerException("side");
            }
            return new ActionWrapperExternal((IActionExternal) statement, side);
        } else {
            throw new IllegalArgumentException("Unknown class or interface " + statement.getClass());
        }
    }

    @Override
    public ActionWrapper[] getPossible() {
        IStatement[] possible = delegate.getPossible();
        boolean andSides = sourcePart != EnumPipePart.CENTER;
        ActionWrapper[] real = new ActionWrapper[possible.length + (andSides ? 5 : 0)];
        for (int i = 0; i < possible.length; i++) {
            real[i] = wrap(possible[i], sourcePart.face);
        }
        if (andSides) {
            EnumPipePart part = sourcePart;
            for (int j = 0; j < 5; j++) {
                int i = j + possible.length;
                part = part.next();
                real[i] = wrap(delegate, part.face);
            }
        }
        return real;
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

        public ActionWrapperInternalSided(IActionInternalSided action, @Nonnull EnumFacing side) {
            super(action, EnumPipePart.fromFacing(side));
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

        public ActionWrapperExternal(IActionExternal action, @Nonnull EnumFacing side) {
            super(action, EnumPipePart.fromFacing(side));
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
            if (neighbourTile == null) {
                return;
            }
            action.actionActivate(neighbourTile, sourcePart.face, source, parameters);
            if (neighbourTile instanceof IActionReceptor) {
                IActionReceptor receptor = (IActionReceptor) neighbourTile;
                receptor.actionActivated(action, parameters);
            }
            isActive = true;
        }
    }
}
