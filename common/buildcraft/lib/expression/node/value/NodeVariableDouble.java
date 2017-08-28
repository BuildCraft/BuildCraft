/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IVariableNode.IVariableNodeDouble;

public class NodeVariableDouble extends NodeVariable implements IVariableNodeDouble {
    public double value;

    public NodeVariableDouble(String name) {
        super(name);
    }

    @Override
    public double evaluate() {
        return value;
    }

    @Override
    public INodeDouble inline() {
        if (isConst) {
            return new NodeConstantDouble(value);
        }
        return this;
    }

    @Override
    public void set(IExpressionNode from) {
        value = ((INodeDouble) from).evaluate();
    }
}
