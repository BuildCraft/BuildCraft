/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IConstantNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;

public class NodeConstantLong implements INodeLong, IConstantNode {
    public static final NodeConstantLong ZERO = new NodeConstantLong(0);
    public final long value;

    public NodeConstantLong(long value) {
        this.value = value;
    }

    @Override
    public long evaluate() {
        return value;
    }

    @Override
    public INodeLong inline() {
        return this;
    }

    @Override
    public String toString() {
        return value + "L";
    }
}
