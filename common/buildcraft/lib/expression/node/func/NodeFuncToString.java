/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncString;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;

public class NodeFuncToString implements INodeFuncString, INodeString {

    private final String name;
    private final IFuncToString func;

    public NodeFuncToString(String name, IFuncToString func) {
        this.name = name;
        this.func = func;
    }

    @Override
    public String evaluate() {
        return func.apply();
    }

    @Override
    public INodeString inline() {
        return this;
    }

    @Override
    public INodeString getNode(INodeStack stack) throws InvalidExpressionException {
        return this;
    }

    @Override
    public String toString() {
        return "[ -> boolean] { " + name + " }";
    }

    public interface IFuncToString {
        String apply();
    }
}
