/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IVariableNode.IVariableNodeString;

public class NodeVariableString implements IVariableNodeString {
    public final String name;
    public String value = "";
    private boolean isConst = false;

    public NodeVariableString(String name) {
        this.name = name;
    }

    @Override
    public void setConstant(boolean isConst) {
        this.isConst = isConst;
    }

    @Override
    public String evaluate() {
        return value;
    }

    @Override
    public INodeString inline() {
        if (isConst) {
            return new NodeConstantString(value);
        }
        return this;
    }

    @Override
    public void set(IExpressionNode from) {
        value = ((INodeString) from).evaluate();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String valueToString() {
        return value;
    }
}
