/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.binary;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.value.NodeConstantString;

public class NodeBinaryString implements INodeString {
    @FunctionalInterface
    public interface BiStringFunction {
        String apply(String l, String r);
    }

    private final INodeString left, right;
    private final BiStringFunction func;
    private final String op;

    public NodeBinaryString(INodeString left, INodeString right, BiStringFunction func, String op) {
        this.left = left;
        this.right = right;
        this.func = func;
        this.op = op;
    }

    @Override
    public String evaluate() {
        return func.apply(left.evaluate(), right.evaluate());
    }

    @Override
    public INodeString inline() {
        return NodeInliningHelper.tryInline(this, left, right, (l, r) -> new NodeBinaryString(l, r, func, op), //
            (l, r) -> new NodeConstantString(func.apply(l.evaluate(), r.evaluate())));
    }

    @Override
    public String toString() {
        return "(" + left + ") " + op + " (" + right + ")";
    }
}
