/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.condition;

import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;

public class NodeConditionalBoolean implements INodeBoolean {
    private final INodeBoolean condition;
    private final INodeBoolean ifTrue, ifFalse;

    public NodeConditionalBoolean(INodeBoolean condition, INodeBoolean ifTrue, INodeBoolean ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public boolean evaluate() {
        return condition.evaluate() ? ifTrue.evaluate() : ifFalse.evaluate();
    }

    @Override
    public INodeBoolean inline() {
        INodeBoolean c = condition.inline();
        INodeBoolean t = ifTrue.inline();
        INodeBoolean f = ifFalse.inline();
        if (c instanceof NodeConstantBoolean && t instanceof NodeConstantBoolean && f instanceof NodeConstantBoolean) {
            return NodeConstantBoolean.get(((NodeConstantBoolean) c).value ? ((NodeConstantBoolean) t).value : ((NodeConstantBoolean) f).value);
        } else if (c != condition || t != ifTrue || f != ifFalse) {
            return new NodeConditionalBoolean(c, t, f);
        } else {
            return this;
        }
    }
}
