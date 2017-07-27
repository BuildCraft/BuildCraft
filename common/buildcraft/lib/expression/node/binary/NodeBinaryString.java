/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.binary;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.node.value.NodeConstantObject;

public class NodeBinaryString implements INodeObject<String> {
    @FunctionalInterface
    public interface BiStringFunction {
        String apply(String l, String r);
    }

    private final INodeObject<String> left, right;
    private final BiStringFunction func;
    private final String op;

    public NodeBinaryString(INodeObject<String> left, INodeObject<String> right, BiStringFunction func, String op) {
        this.left = left;
        this.right = right;
        this.func = func;
        this.op = op;
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public String evaluate() {
        return func.apply(left.evaluate(), right.evaluate());
    }

    @Override
    public INodeObject<String> inline() {
        return NodeInliningHelper.tryInline(this, left, right, (l, r) -> new NodeBinaryString(l, r, func, op), //
            (l, r) -> new NodeConstantObject<>(String.class, func.apply(l.evaluate(), r.evaluate())));
    }

    @Override
    public String toString() {
        return "(" + left + ") " + op + " (" + right + ")";
    }
}
