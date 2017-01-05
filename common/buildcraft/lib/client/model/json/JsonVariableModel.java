package buildcraft.lib.client.model.json;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;

import com.google.gson.*;

import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import buildcraft.lib.client.model.ResourceLoaderContext;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.InternalCompiler;
import buildcraft.lib.expression.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.NodeType;
import buildcraft.lib.expression.node.value.IVariableNode;
import buildcraft.lib.expression.node.value.NodeUpdatable;
import buildcraft.lib.misc.JsonUtil;

/** {@link JsonModel} but any element can change depening on variables. */
public class JsonVariableModel {
    // Never allow ao or textures to be variable - they need to be hardcoded so that we can stitch them
    public final boolean ambientOcclusion;
    public final Map<String, String> textures;
    public final Map<String, NodeUpdatable> variables;
    public final JsonModelRule[] rules;
    private final NodeUpdatable[] variablesArray;
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

    private static JsonVariableModelPart[] deserializePartArray(JsonObject json, String member, FunctionContext fnCtx, ResourceLoaderContext ctx, boolean require) {
        if (!json.has(member)) {
            if (require) {
                throw new JsonSyntaxException("Did not have '" + member + "' in '" + json + "'");
            } else {
                return new JsonVariableModelPart[0];
            }
        }
        JsonElement elem = json.get(member);
        if (!elem.isJsonArray()) {
            throw new JsonSyntaxException("Expected an array, got '" + elem + "'");
        }
        JsonArray array = elem.getAsJsonArray();
        JsonVariableModelPart[] to = new JsonVariableModelPart[array.size()];
        for (int i = 0; i < to.length; i++) {
            to[i] = JsonVariableModelPart.deserialiseModelPart(array.get(i), fnCtx, ctx);
        }
        return to;
    }

    public JsonVariableModel(JsonObject obj, FunctionContext fnCtx, ResourceLoaderContext ctx) throws JsonParseException {
        boolean ambf = false;
        Map<String, String> texturesP = new HashMap<>();
        variables = new HashMap<>();
        List<JsonVariableModelPart> cutout = new ArrayList<>();
        List<JsonVariableModelPart> translucent = new ArrayList<>();
        List<JsonModelRule> rulesP = new ArrayList<>();

        if (obj.has("values")) {
            fnCtx = new FunctionContext(fnCtx);
            putVariables(JsonUtils.getJsonObject(obj, "values"), fnCtx);
        }

        if (obj.has("parent")) {
            String parentName = JsonUtils.getString(obj, "parent");
            parentName += ".json";
            ResourceLocation from = new ResourceLocation(parentName);
            JsonVariableModel parent;
            try {
                parent = deserialize(from, fnCtx, ctx);
            } catch (IOException e) {
                throw new JsonParseException("Didn't find the parent '" + parentName + "'!", e);
            }
            ambf = parent.ambientOcclusion;
            if (!JsonUtils.getBoolean(obj, "textures_reset", false)) {
                texturesP.putAll(parent.textures);
            }
            variables.putAll(parent.variables);
            if (!JsonUtils.getBoolean(obj, "cutout_replace", false)) {
                Collections.addAll(cutout, parent.cutoutElements);
            }
            if (!JsonUtils.getBoolean(obj, "translucent_replace", false)) {
                Collections.addAll(translucent, parent.translucentElements);
            }
            if (!JsonUtils.getBoolean(obj, "rules_replace", false)) {
                Collections.addAll(rulesP, parent.rules);
            }
        }

        ambientOcclusion = JsonUtils.getBoolean(obj, "ambientocclusion", ambf);
        texturesP.putAll(JsonUtil.deserializeStringMap(obj, "textures"));
        textures = texturesP;
        if (obj.has("variables")) {
            fnCtx = new FunctionContext(fnCtx);
            putVariables(JsonUtils.getJsonObject(obj, "variables"), fnCtx);
        }
        variablesArray = variables.values().toArray(new NodeUpdatable[variables.size()]);

        boolean require = cutout.isEmpty() && translucent.isEmpty();
        if (obj.has("elements")) {
            Collections.addAll(cutout, deserializePartArray(obj, "elements", fnCtx, ctx, require));
        } else {
            Collections.addAll(cutout, deserializePartArray(obj, "cutout", fnCtx, ctx, require));
            Collections.addAll(translucent, deserializePartArray(obj, "translucent", fnCtx, ctx, require));
        }
        cutoutElements = cutout.toArray(new JsonVariableModelPart[cutout.size()]);
        translucentElements = translucent.toArray(new JsonVariableModelPart[translucent.size()]);

        if (obj.has("rules")) {
            JsonElement elem = obj.get("rules");
            if (!elem.isJsonArray()) throw new JsonSyntaxException("Expected an array, got " + elem + " for 'rules'");
            JsonArray arr = elem.getAsJsonArray();
            for (int i = 0; i < arr.size(); i++) {
                rulesP.add(JsonModelRule.deserialize(arr.get(i), fnCtx, ctx));
            }
        }
        rules = rulesP.toArray(new JsonModelRule[rulesP.size()]);
    }

    private void putVariables(JsonObject values, FunctionContext fnCtx) {
        for (Entry<String, JsonElement> entry : values.entrySet()) {
            String name = entry.getKey();
            name = name.toLowerCase(Locale.ROOT);
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
            IVariableNode varNode = NodeType.getType(node).makeVariableNode();
            if (variables.containsKey(name)) {
                NodeUpdatable existing = variables.get(name);
                existing.setSource(varNode);
            } else {
                variables.put(name, new NodeUpdatable(node, varNode));
            }
            fnCtx.putVariable(name, varNode);
        }
    }

    public void refreshLocalVariables() {
        for (NodeUpdatable updatable : variablesArray) {
            updatable.refresh();
        }
    }
}
