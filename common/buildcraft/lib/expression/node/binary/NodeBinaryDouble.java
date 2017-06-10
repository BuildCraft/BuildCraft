/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.binary;

import java.util.function.DoubleBinaryOperator;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.node.value.NodeConstantDouble;

public class NodeBinaryDouble implements INodeDouble {
    private final INodeDouble left, right;
    private final DoubleBinaryOperator func;
    private final String op;

    public NodeBinaryDouble(INodeDouble left, INodeDouble right, DoubleBinaryOperator func, String op) {
        this.left = left;
        this.right = right;
        this.func = func;
        this.op = op;
    }

    @Override
    public double evaluate() {
        return func.applyAsDouble(left.evaluate(), right.evaluate());
    }

    @Override
    public INodeDouble inline() {
        return NodeInliningHelper.tryInline(this, left, right, (l, r) -> new NodeBinaryDouble(l, r, func, op), //
            (l, r) -> new NodeConstantDouble(func.applyAsDouble(l.evaluate(), r.evaluate())));
    }

    @Override
    public String toString() {
        return "(" + left + ") " + op + " (" + right + ")";
    }
}
