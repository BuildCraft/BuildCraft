/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncDouble;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;

public class NodeFuncToDouble implements INodeFuncDouble, INodeDouble {

    private final String name;
    private final IFuncToDouble func;

    public NodeFuncToDouble(String name, IFuncToDouble func) {
        this.name = name;
        this.func = func;
    }

    @Override
    public double evaluate() {
        return func.apply();
    }

    @Override
    public INodeDouble inline() {
        return this;
    }

    @Override
    public INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {
        return this;
    }

    @Override
    public String toString() {
        return "[ -> double] { " + name + " }";
    }

    public interface IFuncToDouble {
        double apply();
    }
}
