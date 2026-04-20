/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression.node.value;

import ct.buildcraft.lib.expression.api.IDependancyVisitor;
import ct.buildcraft.lib.expression.api.IDependantNode;
import ct.buildcraft.lib.expression.api.IExpressionNode;
import ct.buildcraft.lib.expression.api.IVariableNode.IVariableNodeDouble;

public class NodeVariableDouble extends NodeVariable implements IVariableNodeDouble, IDependantNode {
    public double value;
    private INodeDouble src;

    public NodeVariableDouble(String name) {
        super(name);
    }

    @Override
    public double evaluate() {
        return src != null ? src.evaluate() : value;
    }

    @Override
    public INodeDouble inline() {
        if (isConst) {
            return new NodeConstantDouble(value);
        } else if (src != null) {
            return src.inline();
        }
        return this;
    }

    @Override
    public void set(double value) {
        this.value = value;
    }

    @Override
    public void setConstantSource(IExpressionNode source) {
        if (src != null) {
            throw new IllegalStateException("Already have a constant source");
        }
        src = (INodeDouble) source;
    }

    @Override
    public void visitDependants(IDependancyVisitor visitor) {
        if (src != null) {
            visitor.dependOn(src);
        } else {
            visitor.dependOnExplictly(this);
        }
    }
}
