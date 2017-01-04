package buildcraft.lib.client.model.json;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;

import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import buildcraft.lib.client.model.ResourceLoaderContext;
import buildcraft.lib.expression.ExpressionDebugManager;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.InternalCompiler;
import buildcraft.lib.expression.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.value.*;
import buildcraft.lib.misc.JsonUtil;

/** {@link JsonModel} but any element can change depening on variables. */
public class JsonVariableModel {
    // Never allow ao or textures to be variable - they need to be hardcoded so that we can stitch them
    public final boolean ambientOcclusion;
    public final Map<String, String> textures;
    public final List<NodeUpdatable> variables;
    public final JsonVariableModelPart[] cutoutElements, translucentElements;

    public static JsonVariableModel deserialize(ResourceLocation from, FunctionContext fnCtx) throws JsonParseException, IOException {
        return deserialize(from, fnCtx, new ResourceLoaderContext());
    }

    public static JsonVariableModel deserialize(ResourceLocation from, FunctionContext fnCtx, ResourceLoaderContext ctx) throws JsonParseException, IOException {
        try (InputStreamReader isr = ctx.startLoading(from)) {
            return new JsonVariableModel(new Gson().fromJson(isr, JsonObject.class), fnCtx, ctx);
        } finally {
            ctx.finishLoading();
        }
    }

    public static JsonVariableModelPart deserializePart(JsonElement json, FunctionContext fnCtx, ResourceLoaderContext ctx) throws JsonParseException {
        // TODO: add different classes based on type
        return JsonVariableModelPart.deserialiseModelPart(json, fnCtx, ctx);
    }

    private static JsonVariableModelPart[] deserializePartArray(JsonObject json, String member, FunctionContext fnCtx, ResourceLoaderContext ctx) {
        if (!json.has(member)) {
            throw new JsonSyntaxException("Did not have '" + member + "' in '" + json + "'");
        }
        JsonElement elem = json.get(member);
        if (!elem.isJsonArray()) {
            throw new JsonSyntaxException("Expected an array, got '" + elem + "'");
        }
        JsonArray array = elem.getAsJsonArray();
        JsonVariableModelPart[] to = new JsonVariableModelPart[array.size()];
        for (int i = 0; i < to.length; i++) {
            to[i] = deserializePart(array.get(i), fnCtx, ctx);
        }
        return to;
    }

    public JsonVariableModel(JsonObject obj, FunctionContext fnCtx, ResourceLoaderContext ctx) throws JsonParseException {
        ambientOcclusion = JsonUtils.getBoolean(obj, "ambientocclusion", false);
        textures = JsonUtil.deserializeStringMap(obj, "textures");
        if (obj.has("variables")) {
            fnCtx = new FunctionContext(fnCtx);
            JsonElement var = obj.get("variables");
            if (!var.isJsonObject()) throw new JsonSyntaxException("Expected an object, got " + var + " for 'variables'");
            JsonObject vars = var.getAsJsonObject();
            variables = new ArrayList<>(vars.entrySet().size());
            for (Entry<String, JsonElement> entry : vars.entrySet()) {
                String name = entry.getKey();
                if (fnCtx.hasLocalVariable(name)) {
                    throw new JsonSyntaxException("Duplicate local variable '" + name + "'");
                }
                JsonElement value = entry.getValue();
                if (!value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                    throw new JsonSyntaxException("Expected a string, got " + value + " for the variable '" + name + "'");
                }
                String expression = value.getAsString();
                IExpressionNode node;
                try {
                    node = InternalCompiler.compileExpression(expression, fnCtx);
                } catch (InvalidExpressionException e) {
                    throw new JsonSyntaxException("Invalid expression", e);
                }
                IVariableNode varNode;
                if (node instanceof INodeLong) {
                    varNode = new NodeVariableLong();
                } else if (node instanceof INodeBoolean) {
                    varNode = new NodeVariableLong();
                } else if (node instanceof INodeDouble) {
                    varNode = new NodeVariableDouble();
                } else if (node instanceof INodeString) {
                    varNode = new NodeVariableString();
                } else {
                    ExpressionDebugManager.debugNodeClass(node.getClass());
                    throw new IllegalStateException("Unknown node class detected! " + node.getClass());
                }
                variables.add(new NodeUpdatable(node, varNode));
                fnCtx.putVariable(name, varNode);
            }
        } else {
            variables = ImmutableList.of();
        }

        if (obj.has("elements")) {
            cutoutElements = deserializePartArray(obj, "elements", fnCtx, ctx);
            translucentElements = new JsonVariableModelPart[0];
        } else {
            cutoutElements = deserializePartArray(obj, "cutout", fnCtx, ctx);
            translucentElements = deserializePartArray(obj, "translucent", fnCtx, ctx);
        }
    }

    public void refreshLocalVariables() {
        for (NodeUpdatable updatable : variables) {
            updatable.refresh();
        }
    }
}
