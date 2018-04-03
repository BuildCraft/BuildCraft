/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.func;

import java.util.function.Supplier;

import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncObject;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;

public class NodeFuncToObject<T> implements INodeFuncObject<T>, INodeObject<T> {

    private final String name;
    private final Class<T> type;
    private final Supplier<T> func;

    public NodeFuncToObject(String name, Class<T> type, Supplier<T> func) {
        this.name = name;
        this.type = type;
        this.func = func;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public T evaluate() {
        return func.get();
    }

    @Override
    public INodeObject<T> getNode(INodeStack stack) throws InvalidExpressionException {
        return this;
    }

    @Override
    public String toString() {
        return "[ -> " + getType() + "] { " + name + " }";
    }
}
