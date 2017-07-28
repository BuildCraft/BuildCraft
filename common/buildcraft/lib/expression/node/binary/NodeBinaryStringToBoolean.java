/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.binary;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;

public class NodeBinaryStringToBoolean implements INodeBoolean {
    @FunctionalInterface
    public interface BiStringToBooleanFunction {
        boolean apply(String l, String r);
    }

    private final INodeObject<String> left, right;
    private final BiStringToBooleanFunction func;
    private final String op;

    public NodeBinaryStringToBoolean(INodeObject<String> left, INodeObject<String> right, BiStringToBooleanFunction func, String op) {
        this.left = left;
        this.right = right;
        this.func = func;
        this.op = op;
    }

    @Override
    public boolean evaluate() {
        return func.apply(left.evaluate(), right.evaluate());
    }

    @Override
    public INodeBoolean inline() {
        return NodeInliningHelper.tryInline(this, left, right, (l, r) -> new NodeBinaryStringToBoolean(l, r, func, op), //
            (l, r) -> NodeConstantBoolean.of(func.apply(l.evaluate(), r.evaluate())));
    }

    @Override
    public String toString() {
        return "(" + left + ") " + op + " (" + right + ")";
    }
}
