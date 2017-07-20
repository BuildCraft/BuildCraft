/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.info;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.info.VariableInfo.VariableInfoBoolean;
import buildcraft.lib.expression.info.VariableInfo.VariableInfoDouble;
import buildcraft.lib.expression.info.VariableInfo.VariableInfoLong;
import buildcraft.lib.expression.info.VariableInfo.VariableInfoString;
import buildcraft.lib.expression.node.value.NodeVariableBoolean;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableLong;
import buildcraft.lib.expression.node.value.NodeVariableString;

import java.util.HashMap;
import java.util.Map;

public class ContextInfo {
    public final FunctionContext fnCtx;
    public final Map<String, VariableInfo<?>> variables = new HashMap<>();

    public ContextInfo(FunctionContext fnCtx) {
        this.fnCtx = fnCtx;
    }

    public VariableInfoString createInfoString(String name, NodeVariableString node) {
        VariableInfoString info = new VariableInfoString(node);
        variables.put(name, info);
        return info;
    }

    public VariableInfoString getInfoString(String name) {
        VariableInfo<?> info = variables.get(name);
        if (info instanceof VariableInfoString) {
            return (VariableInfoString) info;
        }
        return null;
    }

    public VariableInfoDouble createInfoDouble(String name, NodeVariableDouble node) {
        VariableInfoDouble info = new VariableInfoDouble(node);
        variables.put(name, info);
        return info;
    }

    public VariableInfoDouble getInfoDouble(String name) {
        VariableInfo<?> info = variables.get(name);
        if (info instanceof VariableInfoDouble) {
            return (VariableInfoDouble) info;
        }
        return null;
    }

    public VariableInfoLong createInfoLong(String name, NodeVariableLong node) {
        VariableInfoLong info = new VariableInfoLong(node);
        variables.put(name, info);
        return info;
    }

    public VariableInfoLong getInfoLong(String name) {
        VariableInfo<?> info = variables.get(name);
        if (info instanceof VariableInfoLong) {
            return (VariableInfoLong) info;
        }
        return null;
    }

    public VariableInfoBoolean createInfoBoolean(String name, NodeVariableBoolean node) {
        VariableInfoBoolean info = new VariableInfoBoolean(node);
        variables.put(name, info);
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
