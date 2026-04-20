/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression.info;

import java.util.HashMap;
import java.util.Map;

import ct.buildcraft.lib.expression.info.VariableInfo.VariableInfoBoolean;
import ct.buildcraft.lib.expression.info.VariableInfo.VariableInfoDouble;
import ct.buildcraft.lib.expression.info.VariableInfo.VariableInfoLong;
import ct.buildcraft.lib.expression.info.VariableInfo.VariableInfoObject;

import ct.buildcraft.lib.expression.FunctionContext;
import ct.buildcraft.lib.expression.node.value.NodeVariableBoolean;
import ct.buildcraft.lib.expression.node.value.NodeVariableDouble;
import ct.buildcraft.lib.expression.node.value.NodeVariableLong;
import ct.buildcraft.lib.expression.node.value.NodeVariableObject;

public class ContextInfo {
    public final FunctionContext fnCtx;
    public final Map<String, VariableInfo<?>> variables = new HashMap<>();

    public ContextInfo(FunctionContext fnCtx) {
        this.fnCtx = fnCtx;
    }

    public <T> VariableInfoObject<T> createInfoObject(NodeVariableObject<T> node) {
        VariableInfoObject<T> info = new VariableInfoObject<>(node);
        variables.put(node.name, info);
        return info;
    }

    public VariableInfoObject<?> getInfoObject(String name) {
        VariableInfo<?> info = variables.get(name);
        if (info instanceof VariableInfoObject) {
            return (VariableInfoObject<?>) info;
        }
        return null;
    }

    public VariableInfoDouble createInfoDouble(NodeVariableDouble node) {
        VariableInfoDouble info = new VariableInfoDouble(node);
        variables.put(node.name, info);
        return info;
    }

    public VariableInfoDouble getInfoDouble(String name) {
        VariableInfo<?> info = variables.get(name);
        if (info instanceof VariableInfoDouble) {
            return (VariableInfoDouble) info;
        }
        return null;
    }

    public VariableInfoLong createInfoLong(NodeVariableLong node) {
        VariableInfoLong info = new VariableInfoLong(node);
        variables.put(node.name, info);
        return info;
    }

    public VariableInfoLong getInfoLong(String name) {
        VariableInfo<?> info = variables.get(name);
        if (info instanceof VariableInfoLong) {
            return (VariableInfoLong) info;
        }
        return null;
    }

    public VariableInfoBoolean createInfoBoolean(NodeVariableBoolean node) {
        VariableInfoBoolean info = new VariableInfoBoolean(node);
        variables.put(node.name, info);
        return info;
    }

    public VariableInfoBoolean getInfoBoolean(String name) {
        VariableInfo<?> info = variables.get(name);
        if (info instanceof VariableInfoBoolean) {
            return (VariableInfoBoolean) info;
        }
        return null;
    }
}
