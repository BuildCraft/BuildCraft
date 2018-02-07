package buildcraft.lib.gui.json;

import buildcraft.api.core.BCLog;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.InternalCompiler;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantDouble;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.IContainingElement;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.elem.GuiElementContainerResizing;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public abstract class ElementType {
    public final String name;

    public ElementType(String name) {
        this.name = name;
    }

    protected abstract IGuiElement deserialize0(BuildCraftJsonGui gui, IGuiPosition parent, JsonGuiInfo info,
        JsonGuiElement json);

    public final IGuiElement deserialize(BuildCraftJsonGui gui, IGuiPosition parent, JsonGuiInfo info,
        JsonGuiElement json) {
        IGuiElement element = deserialize0(gui, parent, info, json);
        if (element instanceof GuiElementSimple) {
            ((GuiElementSimple) element).name = json.fullName;
        }
        gui.context.putConstant(json.fullName + ".pos", IGuiPosition.class, element);
        gui.context.putConstant(json.fullName + ".area", IGuiArea.class, element);
        gui.varData.addNodes(json.createTickableNodes());

        List<IGuiElement> children = new ArrayList<>();
        IContainingElement container;
        if (element instanceof IContainingElement) {
            container = (IContainingElement) element;
        } else {
            container = new GuiElementContainerResizing(gui, element);
            container.getChildElements().add(element);
        }

        addChildren(gui, container.getChildElementPosition(), info, json, "children", children::add);

        // Special case tooltips + help
        if (json.json.has("help") && !(this instanceof ElementTypeHelp)) {
            addType(gui, parent, info, json, "help", children::add, ElementTypeHelp.INSTANCE);
        }

        if (json.json.has("tooltip") && !(this instanceof ElementTypeToolTip)) {
            addType(gui, parent, info, json, "tooltip", children::add, ElementTypeToolTip.INSTANCE);
        }

        if (!children.isEmpty()) {
            element = container;
            container.getChildElements().addAll(children);
            container.calculateSizes();
        }

        return element;
    }

    protected static void addChildren(BuildCraftJsonGui gui, IGuiPosition parent, JsonGuiInfo info, JsonGuiElement json,
        String subName, Consumer<IGuiElement> to) {
        List<JsonGuiElement> children = json.getChildren(subName);
        for (JsonGuiElement child : children) {
            for (JsonGuiElement c : child.iterate(child.context)) {
                String typeName = c.properties.get("type");
                ElementType type = JsonGuiTypeRegistry.TYPES.get(typeName);
                if (type == null) {
                    BCLog.logger.warn("Unknown type " + typeName);
                } else {
                    to.accept(type.deserialize(gui, parent, info, c));
                }
            }
        }
    }

    protected static void addType(BuildCraftJsonGui gui, IGuiPosition parent, JsonGuiInfo info, JsonGuiElement json,
        String subName, Consumer<IGuiElement> to, ElementType type) {
        JsonGuiElement ch = json.getChildElement(subName, json.json.get(subName));
        if (!ch.properties.containsKey("area") && !ch.properties.containsKey("area[0]")
            && !ch.properties.containsKey("pos[0]")) {
            ch.properties.put("area", json.fullName + ".area");
        }
        to.accept(type.deserialize(gui, parent, info, ch));
    }

    public static FunctionContext createContext(JsonGuiElement json) {
        FunctionContext ctx = json.context;

        // if json overrides variables then its ok
        ctx = new FunctionContext(ctx);

        Set<String> args = new HashSet<>();
        // Put in args first
        for (String key : json.properties.keySet()) {
            if (key.startsWith("args.")) {
                String argName = key.substring("args.".length());
                args.add(argName);
                String value = json.properties.get(key);
                try {
                    IExpressionNode node = InternalCompiler.compileExpression(value, ctx);
                    ctx.putVariable(argName, NodeTypes.createConstantNode(node));
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
                ctx.putVariable(key, NodeTypes.createConstantNode(node));
            } catch (InvalidExpressionException e) {
                // Ignore the error
                // BCLog.logger.info("Failed to compile expression for " + key + " because " + e.getMessage());
            }
        }
        return ctx;
    }

    public static void inheritProperty(JsonGuiElement json, String from, String to) {
        if (!json.properties.containsKey(to) && json.properties.containsKey(from)) {
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
        return (int) getEquationLong(json, member, ctx).evaluate();
    }

    public static INodeLong getEquationLong(JsonGuiElement json, String member, FunctionContext ctx) {
        String eqn = json.properties.get(member);
        if (eqn == null) {
            return NodeConstantLong.ZERO;
        }
        try {
            return GenericExpressionCompiler.compileExpressionLong(eqn, ctx);
        } catch (InvalidExpressionException iee) {
            throw new JsonSyntaxException(iee);
        }
    }

    public static double resolveEquationDouble(JsonGuiElement json, String member, FunctionContext ctx) {
        return getEquationDouble(json, member, ctx).evaluate();
    }

    public static INodeDouble getEquationDouble(JsonGuiElement json, String member, FunctionContext ctx) {
        String eqn = json.properties.get(member);
        if (eqn == null) {
            return NodeConstantDouble.ZERO;
        }
        try {
            return GenericExpressionCompiler.compileExpressionDouble(eqn, ctx);
        } catch (InvalidExpressionException iee) {
            throw new JsonSyntaxException(iee);
        }
    }

    public static boolean resolveEquationBool(JsonGuiElement json, String member, FunctionContext ctx,
        boolean _default) {
        return getEquationBool(json, member, ctx, _default).evaluate();
    }

    public static INodeBoolean getEquationBool(JsonGuiElement json, String member, FunctionContext ctx,
        boolean _default) {
        String eqn = json.properties.get(member);
        if (eqn == null) {
            return NodeConstantBoolean.of(_default);
        }
        try {
            return GenericExpressionCompiler.compileExpressionBoolean(eqn, ctx);
        } catch (InvalidExpressionException iee) {
            throw new JsonSyntaxException(iee);
        }
    }

    public static IGuiPosition resolvePosition(JsonGuiElement json, String name, IGuiPosition parent,
        FunctionContext ctx) {
        String eqn = json.properties.get(name);
        if (eqn == null) {
            INodeDouble x = getEquationDouble(json, name + "[0]", ctx);
            INodeDouble y = getEquationDouble(json, name + "[1]", ctx);
            return IGuiPosition.create(x, y).offset(parent);
        }
        try {
            return GenericExpressionCompiler.compileExpressionObject(IGuiPosition.class, eqn, ctx).evaluate();
        } catch (InvalidExpressionException e) {
            throw new JsonSyntaxException("Failed to resolve a position for " + json.fullName, e);
        }
    }

    public static IGuiArea resolveArea(JsonGuiElement json, String name, IGuiPosition parent, FunctionContext ctx) {
        String eqn = json.properties.get(name);
        if (eqn == null) {
            INodeDouble x = getEquationDouble(json, name + "[0]", ctx);
            INodeDouble y = getEquationDouble(json, name + "[1]", ctx);
            INodeDouble w = getEquationDouble(json, name + "[2]", ctx);
            INodeDouble h = getEquationDouble(json, name + "[3]", ctx);
            return IGuiArea.create(x, y, w, h).offset(parent);
        }
        try {
            return GenericExpressionCompiler.compileExpressionObject(IGuiArea.class, eqn, ctx).evaluate();
        } catch (InvalidExpressionException e) {
            throw new JsonSyntaxException("Failed to resolve an area for " + json.fullName, e);
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
