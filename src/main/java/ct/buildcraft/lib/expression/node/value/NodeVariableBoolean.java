/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression.node.value;

import ct.buildcraft.lib.expression.api.IDependancyVisitor;
import ct.buildcraft.lib.expression.api.IDependantNode;
import ct.buildcraft.lib.expression.api.IExpressionNode;
import ct.buildcraft.lib.expression.api.IVariableNode.IVariableNodeBoolean;

public class NodeVariableBoolean extends NodeVariable implements IVariableNodeBoolean, IDependantNode {
    public boolean value;
    private INodeBoolean src = null;

    public NodeVariableBoolean(String name) {
        super(name);
    }

    @Override
    public boolean evaluate() {
        return src != null ? src.evaluate() : value;
    }

    @Override
    public INodeBoolean inline() {
        if (isConst) {
            return NodeConstantBoolean.of(value);
        } else if (src != null) {
            return src.inline();
        }
        return this;
    }

    @Override
    public void set(boolean value) {
        this.value = value;
    }

    @Override
    public void setConstantSource(IExpressionNode source) {
        if (src != null) {
            throw new IllegalStateException("Already have a constant source");
        }
        src = (INodeBoolean) source;
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
