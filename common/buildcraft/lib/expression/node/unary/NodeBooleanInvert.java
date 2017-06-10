/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.unary;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;

public class NodeBooleanInvert implements INodeBoolean {
    private final INodeBoolean from;

    public NodeBooleanInvert(INodeBoolean from) {
        this.from = from;
    }

    @Override
    public boolean evaluate() {
        return !from.evaluate();
    }

    @Override
    public INodeBoolean inline() {
        return NodeInliningHelper.tryInline(this, from, NodeBooleanInvert::new, (f) -> NodeConstantBoolean.get(!f.evaluate()));
    }

    @Override
    public String toString() {
        return "!(" + from + ")";
    }
}
