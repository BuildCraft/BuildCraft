/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression.node.cast;

import ct.buildcraft.lib.expression.NodeInliningHelper;
import ct.buildcraft.lib.expression.api.IDependancyVisitor;
import ct.buildcraft.lib.expression.api.IDependantNode;
import ct.buildcraft.lib.expression.api.IExpressionNode;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import ct.buildcraft.lib.expression.node.value.NodeConstantObject;

public class NodeCastToString implements INodeObject<String>, IDependantNode {
    private final IExpressionNode from;

    public NodeCastToString(IExpressionNode from) {
        this.from = from;
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public String evaluate() {
        return from.evaluateAsString();
    }

    @Override
    public INodeObject<String> inline() {
        return NodeInliningHelper.tryInline(this, from, NodeCastToString::new,
            (f) -> new NodeConstantObject<>(String.class, f.evaluateAsString()));
    }

    @Override
    public void visitDependants(IDependancyVisitor visitor) {
        visitor.dependOn(from);
    }

    @Override
    public String toString() {
        return "_to_string(" + from + ")";
    }
}
