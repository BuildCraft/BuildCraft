/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IConstantNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;

public class NodeConstantString implements INodeString, IConstantNode {
    public static final NodeConstantString EMPTY = new NodeConstantString("");

    public final String value;

    public NodeConstantString(String value) {
        this.value = value;
    }

    @Override
    public String evaluate() {
        return value;
    }

    @Override
    public INodeString inline() {
        return this;
    }

    @Override
    public String toString() {
        return "'" + value + "'";
    }
}
