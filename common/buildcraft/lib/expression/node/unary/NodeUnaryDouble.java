/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.unary;

import java.util.function.DoubleUnaryOperator;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.node.value.NodeConstantDouble;

public class NodeUnaryDouble implements INodeDouble {
    private final INodeDouble from;
    private final DoubleUnaryOperator func;
    private final String op;

    public NodeUnaryDouble(INodeDouble from, DoubleUnaryOperator func, String op) {
        this.from = from;
        this.func = func;
        this.op = op;
    }

    @Override
    public double evaluate() {
        return func.applyAsDouble(from.evaluate());
    }

    @Override
    public INodeDouble inline() {
        return NodeInliningHelper.tryInline(this, from, (f) -> new NodeUnaryDouble(f, func, op), //
            (f) -> new NodeConstantDouble(func.applyAsDouble(f.evaluate())));
    }

    @Override
    public String toString() {
        return op + "(" + from + ")";
    }
}
