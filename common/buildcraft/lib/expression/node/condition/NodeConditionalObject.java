/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.condition;

import buildcraft.lib.expression.ExpressionDebugManager;
import buildcraft.lib.expression.api.IConstantNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.node.value.NodeConstantObject;

public class NodeConditionalObject<T> implements INodeObject<T> {
    private final INodeBoolean condition;
    private final INodeObject<T> ifTrue, ifFalse;

    public NodeConditionalObject(INodeBoolean condition, INodeObject<T> ifTrue, INodeObject<T> ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public Class<T> getType() {
        return ifTrue.getType();
    }

    @Override
    public T evaluate() {
        return condition.evaluate() ? ifTrue.evaluate() : ifFalse.evaluate();
    }

    @Override
    public INodeObject<T> inline() {
        ExpressionDebugManager.debugStart("Inlining " + this);
        INodeBoolean c = condition.inline();
        INodeObject<T> t = ifTrue.inline();
        INodeObject<T> f = ifFalse.inline();
        if (c instanceof IConstantNode) {
            boolean cc = c.evaluate();
            INodeObject<T> val;
            if (cc) {
                if (t instanceof IConstantNode) {
                    val = new NodeConstantObject<>(getType(), t.evaluate());
                } else {
                    val = t;
                }
            } else {
                if (f instanceof IConstantNode) {
                    val = new NodeConstantObject<>(getType(), f.evaluate());
                } else {
                    val = f;
                }
            }
            ExpressionDebugManager.debugEnd("Fully inlined to " + val);
            return val;
        } else if (c != condition || t != ifTrue || f != ifFalse) {
            NodeConditionalObject<T> val = new NodeConditionalObject<>(c, t, f);
            ExpressionDebugManager.debugEnd("Partially inlined to " + val);
            return val;
        } else {
            ExpressionDebugManager.debugEnd("Unable to inline at all!");
            return this;
        }
    }

    @Override
    public String toString() {
        return "(" + condition + ") ? (" + ifTrue + ") : (" + ifFalse + ")";
    }
}
