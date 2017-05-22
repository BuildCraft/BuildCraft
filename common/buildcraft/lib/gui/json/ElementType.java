package buildcraft.lib.gui.json;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.google.gson.JsonSyntaxException;

import buildcraft.api.core.BCLog;

import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.InternalCompiler;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeType;
import buildcraft.lib.gui.ContainerBC_Neptune;

public abstract class ElementType<G extends GuiJson<C>, C extends ContainerBC_Neptune> {
    public final String name;

    public ElementType(String name) {
        this.name = name;
    }

    public abstract void addToGui(G gui, JsonGuiInfo info, JsonGuiElement json, Map<String, Supplier<String>> guiProps);

    public static FunctionContext createContext(JsonGuiElement json) {
        FunctionContext ctx = DefaultContexts.createWithAll();

        Set<String> args = new HashSet<>();
        // Put in args first
        for (String key : json.properties.keySet()) {
            if (key.startsWith("args")) {
                String argName = key.substring("args.".length());
                args.add(argName);
                String value = json.properties.get(key);
                try {
                    IExpressionNode node = InternalCompiler.compileExpression(value, ctx);
                    ctx.putVariable(argName, NodeType.createConstantNode(node));
                } catch (InvalidExpressionException e) {
                    // Ignore the error
                    BCLog.logger.info("Failed to compile expression for " + key + " because " + e.getMessage());
                }
            }
        }

        for (String key : json.properties.keySet()) {
            if (key.contains(".") || key.contains("[")) {
                continue;
            }
            String value = json.properties.get(key);
            try {
                IExpressionNode node = InternalCompiler.compileExpression(value, ctx);
                ctx.putVariable(key, NodeType.createConstantNode(node));
            } catch (InvalidExpressionException e) {
                // Ignore the error
                BCLog.logger.info("Failed to compile expression for " + key + " because " + e.getMessage());
            }
        }
        return ctx;
    }

    public static void inheritProperty(JsonGuiElement json, String from, String to) {
        if (!json.properties.containsKey(to)) {
            json.properties.put(to, json.properties.get(from));
        }
    }

    public static String resolveEquation(JsonGuiElement json, String member, FunctionContext ctx) {
        String eqn = json.properties.get(member);
        if (eqn == null) {
            return null;
        }
        try {
            return GenericExpressionCompiler.compileExpressionString(eqn, ctx).evaluate();
        } catch (InvalidExpressionException iee) {
            throw new JsonSyntaxException(iee);
        }
    }

    public static int resolveEquationInt(JsonGuiElement json, String member, FunctionContext ctx) {
        String eqn = json.properties.get(member);
        if (eqn == null) {
            return 0;
        }
        try {
            return (int) GenericExpressionCompiler.compileExpressionLong(eqn, ctx).evaluate();
        } catch (InvalidExpressionException iee) {
            throw new JsonSyntaxException(iee);
        }
    }

    public static double resolveEquationDouble(JsonGuiElement json, String member, FunctionContext ctx) {
        String eqn = json.properties.get(member);
        if (eqn == null) {
            return 0;
        }
        try {
            return GenericExpressionCompiler.compileExpressionDouble(eqn, ctx).evaluate();
        } catch (InvalidExpressionException iee) {
            throw new JsonSyntaxException(iee);
        }
    }

    public static boolean resolveEquationBool(JsonGuiElement json, String member, FunctionContext ctx, boolean _default) {
        String eqn = json.properties.get(member);
        if (eqn == null) {
            return _default;
        }
        try {
            return GenericExpressionCompiler.compileExpressionBoolean(eqn, ctx).evaluate();
        } catch (InvalidExpressionException iee) {
            throw new JsonSyntaxException(iee);
        }
    }

    public static SrcTexture resolveTexture(JsonGuiInfo fallback, JsonGuiElement json, String memberPrefix) {
        String origin;
        int texSize;
        if (json.properties.containsKey(memberPrefix + ".texture")) {
            origin = json.properties.get(memberPrefix + ".texture");
            texSize = 256;
        } else if (json.properties.containsKey(memberPrefix + ".sprite")) {
            origin = json.properties.get(memberPrefix + ".sprite");
            texSize = 16;
        } else {
            origin = fallback.defaultTexture;
            texSize = 256;
        }
        return new SrcTexture(origin, texSize);
    }

    public static class SrcTexture {
        public final String origin;
        public final int texSize;

        public SrcTexture(String origin, int texSize) {
            this.origin = origin;
            this.texSize = texSize;
        }
    }
}
