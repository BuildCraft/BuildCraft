/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.condition;

import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantLong;

public class NodeConditionalLong implements INodeLong, IDependantNode {
    private final INodeBoolean condition;
    private final INodeLong ifTrue, ifFalse;

    public NodeConditionalLong(INodeBoolean condition, INodeLong ifTrue, INodeLong ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public long evaluate() {
        return condition.evaluate() ? ifTrue.evaluate() : ifFalse.evaluate();
    }

    @Override
    public INodeLong inline() {
        INodeBoolean c = condition.inline();
        INodeLong t = ifTrue.inline();
        INodeLong f = ifFalse.inline();
        if (c instanceof NodeConstantBoolean && t instanceof NodeConstantLong && f instanceof NodeConstantLong) {
            return new NodeConstantLong(
                ((NodeConstantBoolean) c).value ? ((NodeConstantLong) t).value : ((NodeConstantLong) f).value);
        } else if (c != condition || t != ifTrue || f != ifFalse) {
            return new NodeConditionalLong(c, t, f);
        } else if (c instanceof NodeConstantBoolean) {
            return ((NodeConstantBoolean) c).value ? t : f;
        } else {
            return this;
        }
    }

    @Override
    public void visitDependants(IDependancyVisitor visitor) {
        visitor.dependOn(condition, ifTrue, ifFalse);
    }

    @Override
    public String toString() {
        return "(" + condition + ") ? (" + ifTrue + ") : (" + ifFalse + ")";
    }
}
