/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression.node.condition;

import ct.buildcraft.lib.expression.api.IDependancyVisitor;
import ct.buildcraft.lib.expression.api.IDependantNode;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import ct.buildcraft.lib.expression.node.value.NodeConstantBoolean;
import ct.buildcraft.lib.expression.node.value.NodeConstantDouble;

public class NodeConditionalDouble implements INodeDouble, IDependantNode {
    private final INodeBoolean condition;
    private final INodeDouble ifTrue, ifFalse;

    public NodeConditionalDouble(INodeBoolean condition, INodeDouble ifTrue, INodeDouble ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public double evaluate() {
        return condition.evaluate() ? ifTrue.evaluate() : ifFalse.evaluate();
    }

    @Override
    public INodeDouble inline() {
        INodeBoolean c = condition.inline();
        INodeDouble t = ifTrue.inline();
        INodeDouble f = ifFalse.inline();
        if (c instanceof NodeConstantBoolean && t instanceof NodeConstantDouble && f instanceof NodeConstantDouble) {
            return new NodeConstantDouble(
                ((NodeConstantBoolean) c).value ? ((NodeConstantDouble) t).value : ((NodeConstantDouble) f).value);
        } else if (c != condition || t != ifTrue || f != ifFalse) {
            return new NodeConditionalDouble(c, t, f);
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
