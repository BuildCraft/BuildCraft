package buildcraft.lib.expression.info;

import java.util.HashMap;
import java.util.Map;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.info.VariableInfo.VariableInfoDouble;
import buildcraft.lib.expression.info.VariableInfo.VariableInfoLong;
import buildcraft.lib.expression.info.VariableInfo.VariableInfoString;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableLong;
import buildcraft.lib.expression.node.value.NodeVariableString;

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

    public VariableInfoLong createInfoString(String name, NodeVariableLong node) {
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
}
