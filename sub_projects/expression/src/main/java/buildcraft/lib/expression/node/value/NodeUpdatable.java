/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.value;

import javax.annotation.Nullable;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.NodeTypes;

public class NodeUpdatable implements ITickableNode, ITickableNode.Source {
    public final String name;
    public final NodeVariable variable;
    private IExpressionNode source;
    private boolean finalised;

    public NodeUpdatable(String name, IExpressionNode source) {
        this.name = name;
        this.variable = NodeTypes.makeVariableNode(NodeTypes.getType(source), name);
        setSource(source);
    }

    @Override
    public void refresh() {
        variable.set(source);
    }

    @Override
    public void tick() {
        refresh();
    }

    @Override
    public ITickableNode createTickable() {
        return this;
    }

    @Override
    public void setSource(IExpressionNode source) {
        this.source = source;
        refresh();
    }

    public void makeSourceConstant() {
        if (source == null) {
            throw new IllegalStateException("Source not set yet!");
        }
        finalised = true;
        variable.setConstantSource(source);
    }

    @Nullable
    public IExpressionNode getConstantSource() {
        return finalised ? source : null;
    }
}
