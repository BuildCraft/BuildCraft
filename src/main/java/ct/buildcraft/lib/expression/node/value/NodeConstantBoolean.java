/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression.node.value;

import ct.buildcraft.lib.expression.api.IConstantNode;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;

public enum NodeConstantBoolean implements INodeBoolean, IConstantNode {
    TRUE(true),
    FALSE(false);

    public final boolean value;

    NodeConstantBoolean(boolean b) {
        this.value = b;
    }

    /**
     * @deprecated Use {@link #of(boolean)} instead
     */
    public static NodeConstantBoolean get(boolean value) {
        return of(value);
    }

    public static NodeConstantBoolean of(boolean value) {
        if (value) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    @Override
    public boolean evaluate() {
        return value;
    }

    @Override
    public INodeBoolean inline() {
        return this;
    }

    public NodeConstantBoolean invert() {
        return of(!value);
    }
}
