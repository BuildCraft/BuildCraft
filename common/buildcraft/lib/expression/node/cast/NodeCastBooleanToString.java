/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.cast;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.value.NodeConstantString;

public class NodeCastBooleanToString implements INodeString {
    private final INodeBoolean from;

    public NodeCastBooleanToString(INodeBoolean from) {
        this.from = from;
    }

    @Override
    public String evaluate() {
        return Boolean.toString(from.evaluate());
    }

    @Override
    public INodeString inline() {
        return NodeInliningHelper.tryInline(this, from, NodeCastBooleanToString::new, (f) -> new NodeConstantString(Boolean.toString(f.evaluate())));
    }

    @Override
    public String toString() {
        return "_to_string(" + from + ")";
    }
}
