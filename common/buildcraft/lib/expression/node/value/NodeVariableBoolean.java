/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IVariableNode.IVariableNodeBoolean;

public class NodeVariableBoolean implements IVariableNodeBoolean {
    public final String name;
    public boolean value;
    private boolean isConst = false;

    public NodeVariableBoolean(String name) {
        this.name = name;
    }

    @Override
    public void setConstant(boolean isConst) {
        this.isConst = isConst;
    }

    @Override
    public boolean evaluate() {
        return value;
    }

    @Override
    public INodeBoolean inline() {
        if (isConst) {
            return NodeConstantBoolean.get(value);
        }
        return this;
    }

    @Override
    public String toString() {
        return name + " = " + valueToString();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void set(IExpressionNode from) {
        value = ((INodeBoolean) from).evaluate();
    }

    @Override
    public String valueToString() {
        return Boolean.toString(value);
    }
}
