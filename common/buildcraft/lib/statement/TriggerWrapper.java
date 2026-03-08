/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.statement;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.statements.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

import javax.annotation.Nonnull;

/** Wrapper class around ITriggerInternal to allow for treating all triggers as internal triggers. It also provides the
 * background colour for sides. */
public abstract class TriggerWrapper extends StatementWrapper implements ITriggerInternal {

    public TriggerWrapper(IStatement delegate, EnumPipePart sourcePart) {
        super(delegate, sourcePart);
    }

    public static TriggerWrapper wrap(IStatement statement, Direction side) {
        if (statement == null) {
            return null;
        } else if (statement instanceof TriggerWrapper) {
            return (TriggerWrapper) statement;
        } else if (statement instanceof ITriggerInternal && side == null) {
            return new TriggerWrapperInternal((ITriggerInternal) statement);
        } else if (statement instanceof ITriggerInternalSided) {
            if (side == null) {
                throw new NullPointerException("side");
            }
            return new TriggerWrapperInternalSided((ITriggerInternalSided) statement, side);
        } else if (statement instanceof ITriggerExternal) {
            if (side == null) {
                throw new NullPointerException("side");
            }
            return new TriggerWrapperExternal((ITriggerExternal) statement, side);
        } else {
            throw new IllegalArgumentException("Unknown class or interface " + statement.getClass());
        }
    }

    @Override
    public TriggerWrapper[] getPossible() {
        IStatement[] possible = delegate.getPossible();
        boolean andSides = sourcePart != EnumPipePart.CENTER;
        TriggerWrapper[] real = new TriggerWrapper[possible.length + (andSides ? 5 : 0)];
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

    public static class TriggerWrapperInternal extends TriggerWrapper {
        public final ITriggerInternal trigger;

        public TriggerWrapperInternal(ITriggerInternal trigger) {
            super(trigger, EnumPipePart.CENTER);
            this.trigger = trigger;
        }

        @Override
        public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
            return trigger.isTriggerActive(source, parameters);
        }
    }

    public static class TriggerWrapperInternalSided extends TriggerWrapper {
        public final ITriggerInternalSided trigger;

        public TriggerWrapperInternalSided(ITriggerInternalSided trigger, @Nonnull Direction side) {
            super(trigger, EnumPipePart.fromFacing(side));
            this.trigger = trigger;
        }

        @Override
        public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
            return trigger.isTriggerActive(sourcePart.face, source, parameters);
        }
    }

    public static class TriggerWrapperExternal extends TriggerWrapper {
        public final ITriggerExternal trigger;

        public TriggerWrapperExternal(ITriggerExternal trigger, @Nonnull Direction side) {
            super(trigger, EnumPipePart.fromFacing(side));
            this.trigger = trigger;
        }

        @Override
        public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
            TileEntity tile = getNeighbourTile(source);
            if (tile == null) {
                return false;
            }
            if (tile instanceof ITriggerExternalOverride) {
                ITriggerExternalOverride override = (ITriggerExternalOverride) tile;
                ITriggerExternalOverride.Result result =
                        override.override(sourcePart.face, source, trigger, parameters);
                if (result == ITriggerExternalOverride.Result.FALSE) {
                    return false;
                } else if (result == ITriggerExternalOverride.Result.TRUE) {
                    return true;
                }
            }
            return trigger.isTriggerActive(tile, sourcePart.face, source, parameters);
        }
    }
}
