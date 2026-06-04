/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.silicon.gate;

import java.util.List;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.statements.IStatement;

import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.statement.StatementContext;

public class GateContext<T extends IStatement> implements StatementContext<T> {

    public final List<GateGroup<T>> groups;

    public GateContext(List<GateGroup<T>> groups) {
        this.groups = groups;
    }

    @Override
    public List<? extends StatementGroup<T>> getAllPossible() {
        return groups;
    }

    public static class GateGroup<T extends IStatement> implements StatementGroup<T> {
        public final EnumPipePart part;
        public final List<T> statements;

        public GateGroup(EnumPipePart part, List<T> statements) {
            this.part = part;
            this.statements = statements;
        }

        @Override
        public List<T> getValues() {
            return statements;
        }

        @Override
        public ISimpleDrawable getSourceIcon() {
            return null;
        }

        @Override
        public int getLedgerColour() {
            if (part == EnumPipePart.CENTER) {
                return 0;
            }
            // STUB(R.Chen): ColourUtil.getColourForSide not in libLeaf — return 0 until ColourUtil is migrated.
            return 0;
        }
    }
}
