/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression.node.func;

import ct.buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import ct.buildcraft.lib.expression.api.INodeFunc.INodeFuncLong;
import ct.buildcraft.lib.expression.api.INodeStack;
import ct.buildcraft.lib.expression.api.InvalidExpressionException;

public class NodeFuncToLong implements INodeFuncLong, INodeLong {

    private final String name;
    private final IFuncToLong func;

    public NodeFuncToLong(String name, IFuncToLong func) {
        this.name = name;
        this.func = func;
    }

    @Override
    public long evaluate() {
        return func.apply();
    }

    @Override
    public INodeLong inline() {
        return this;
    }

    @Override
    public INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
        return this;
    }

    @Override
    public String toString() {
        return "[ -> long] { " + name + " }";
    }

    public interface IFuncToLong {
        long apply();
    }
}
