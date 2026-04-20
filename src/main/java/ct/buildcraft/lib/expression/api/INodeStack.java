/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression.api;

import ct.buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeObject;

public interface INodeStack {
    INodeLong popLong() throws InvalidExpressionException;

    INodeDouble popDouble() throws InvalidExpressionException;

    INodeBoolean popBoolean() throws InvalidExpressionException;

    <T> INodeObject<T> popObject(Class<T> clazz) throws InvalidExpressionException;

    default IExpressionNode pop(Class<?> type) throws InvalidExpressionException {
        if (type == long.class) return popLong();
        if (type == double.class) return popDouble();
        if (type == boolean.class) return popBoolean();
        return popObject(type);
    }
}
