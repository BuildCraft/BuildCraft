/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.cast;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.node.value.NodeConstantDouble;

public class NodeCastLongToDouble implements INodeDouble, IDependantNode {
    private final INodeLong from;

    public NodeCastLongToDouble(INodeLong from) {
        this.from = from;
    }

    @Override
    public double evaluate() {
        return from.evaluate();
    }

    @Override
    public INodeDouble inline() {
        return NodeInliningHelper.tryInline(this, from, NodeCastLongToDouble::new,
            (f) -> new NodeConstantDouble(f.evaluate()));
    }

    @Override
    public void visitDependants(IDependancyVisitor visitor) {
        visitor.dependOn(from);
    }

    @Override
    public String toString() {
        return "_long_to_double( " + from + " )";
    }
}
