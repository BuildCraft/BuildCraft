/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IVariableNode.IVariableNodeLong;

public class NodeVariableLong extends NodeVariable implements IVariableNodeLong {
    public long value;

    public NodeVariableLong(String name) {
        super(name);
    }

    @Override
    public long evaluate() {
        return value;
    }

    @Override
    public INodeLong inline() {
        if (isConst) {
            return new NodeConstantLong(value);
        }
        return this;
    }

    @Override
    public void set(IExpressionNode from) {
        value = ((INodeLong) from).evaluate();
    }
}
