package buildcraft.lib.client.model.json;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;

import com.google.gson.*;

import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.ResourceLoaderContext;
import buildcraft.lib.client.sprite.ISprite;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.InternalCompiler;
import buildcraft.lib.expression.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.NodeType;
import buildcraft.lib.expression.node.value.ITickableNode;
import buildcraft.lib.expression.node.value.NodeStateful;
import buildcraft.lib.expression.node.value.NodeStateful.IGetterFunc;
import buildcraft.lib.expression.node.value.NodeUpdatable;
import buildcraft.lib.misc.JsonUtil;

/** {@link JsonModel} but any element can change depening on variables. */
public class JsonVariableModel {
    // Never allow ao or textures to be variable - they need to be hardcoded so that we can stitch them
    public final boolean ambientOcclusion;
    public final Map<String, JsonTexture> textures;
    public final Map<String, ITickableNode.Source> variables;
    public final JsonModelRule[] rules;
    private final ITickableNode.Source[] variablesArray;
    public final JsonVariableModelPart[] cutoutElements, translucentElements;

    public static JsonVariableModel deserialize(ResourceLocation from, FunctionContext fnCtx) throws JsonParseException, IOException {
        return deserialize(from, fnCtx, new ResourceLoaderContext());
    }

    public static JsonVariableModel deserialize(ResourceLocation from, FunctionContext fnCtx, ResourceLoaderContext ctx) throws JsonParseException, IOException {
        try (InputStreamReader isr = ctx.startLoading(from)) {
            return new JsonVariableModel(JsonUtil.inlineCustom(new Gson().fromJson(isr, JsonObject.class)), fnCtx, ctx);
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
        textures = new HashMap<>();
        variables = new LinkedHashMap<>();
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
                textures.putAll(parent.textures);
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
        deserializeTextures(obj.get("textures"));
        if (obj.has("variables")) {
            fnCtx = new FunctionContext(fnCtx);
            putVariables(JsonUtils.getJsonObject(obj, "variables"), fnCtx);
        }
        variablesArray = variables.values().toArray(new ITickableNode.Source[0]);

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

    private void deserializeTextures(JsonElement elem) {
        if (elem == null) return;
        if (!elem.isJsonObject()) {
            throw new JsonSyntaxException("Expected to find an object for 'textures', but found " + elem);
        }
        JsonObject obj = elem.getAsJsonObject();
        for (Entry<String, JsonElement> entry : obj.entrySet()) {
            String name = entry.getKey();
            JsonElement tex = entry.getValue();
            JsonTexture texture;
            if (tex.isJsonPrimitive() && tex.getAsJsonPrimitive().isString()) {
                String location = tex.getAsString();
                texture = new JsonTexture(location);
            } else if (tex.isJsonObject()) {
                texture = new JsonTexture(tex.getAsJsonObject());
            } else {
                throw new JsonSyntaxException("Expected a string or an object, but got " + tex);
            }
            textures.put(name, texture);
        }
    }

    private void putVariables(JsonObject values, FunctionContext fnCtx) {
        for (Entry<String, JsonElement> entry : values.entrySet()) {
            String name = entry.getKey();
            name = name.toLowerCase(Locale.ROOT);
            if (fnCtx.hasLocalVariable(name)) {
                throw new JsonSyntaxException("Duplicate local variable '" + name + "'");
            } else if (fnCtx.getVariable(name) != null) {
                // Allow overriding of higher up variables
                // ...what? Doesn't this disallow overriding existing variables?
                continue;
            }
            JsonElement value = entry.getValue();
            String type = null, getter = null, rounder = null;

            if (value.isJsonObject()) {
                JsonObject objValue = value.getAsJsonObject();
                value = objValue.get("value");
                type = JsonUtils.getString(objValue, "type");
                getter = JsonUtils.getString(objValue, "getter");
                if (objValue.has("rounder")) {
                    rounder = JsonUtils.getString(objValue, "rounder");
                }
            }

            if (!value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                throw new JsonSyntaxException("Expected a string, got " + value + " for the variable '" + name + "'");
            }
            NodeStateful stateful = null;
            if (getter != null) {
                // stateful node
                NodeType nodeType;
                try {
                    nodeType = NodeType.parseType(type);
                } catch (InvalidExpressionException iee) {
                    throw new JsonSyntaxException("Could not parse node type for variable '" + name + "'", iee);
                }
                IGetterFunc getterFunc = parseGetterFunction(getter, fnCtx);
                try {
                    stateful = new NodeStateful(name, nodeType, getterFunc);
                } catch (InvalidExpressionException iee) {
                    throw new JsonSyntaxException("Could not create a getter for the variable '" + name + "'", iee);
                }
                fnCtx.putVariable(name, stateful.getter);
                if (rounder != null) {
                    FunctionContext fnCtx2 = new FunctionContext(fnCtx);
                    fnCtx2.putVariable("last", stateful.last);
                    fnCtx2.putVariable("var", stateful.variable);
                    fnCtx2.putVariable("value", stateful.rounderValue);
                    try {
                        IExpressionNode nodeRounder = InternalCompiler.compileExpression(rounder, fnCtx2);
                        stateful.setRounder(nodeRounder);
                    } catch (InvalidExpressionException iee) {
                        throw new JsonSyntaxException("Could not compile a rounder for the variable '" + name + "'", iee);
                    }
                }
            }

            String expression = value.getAsString();
            IExpressionNode node;
            try {
                node = InternalCompiler.compileExpression(expression, fnCtx);
            } catch (InvalidExpressionException e) {
                throw new JsonSyntaxException("Invalid expression " + expression, e);
            }
            if (variables.containsKey(name)) {
                ITickableNode.Source existing = variables.get(name);
                existing.setSource(node);
            } else if (stateful != null) {
                stateful.setSource(node);
                variables.put(name, stateful);
            } else {
                NodeUpdatable nodeUpdatable = new NodeUpdatable(name, node);
                variables.put(name, nodeUpdatable);
                fnCtx.putVariable(name, nodeUpdatable.variable);
            }
        }
    }

    private static IGetterFunc parseGetterFunction(String getter, FunctionContext fnCtx) {
        if ("interpolate_partial_ticks".equalsIgnoreCase(getter)) {
            return NodeStateful.GetterType.INTERPOLATE_PARTIAL_TICKS;
        }
        if ("last".equalsIgnoreCase(getter)) {
            return NodeStateful.GetterType.USE_LAST;
        }
        if ("var".equalsIgnoreCase(getter)) {
            return NodeStateful.GetterType.USE_VAR;
        }
        return (var, last) -> {
            FunctionContext fnCtx2 = new FunctionContext(fnCtx);
            fnCtx2.putVariable("var", var);
            fnCtx2.putVariable("last", last);
            return InternalCompiler.compileExpression(getter, fnCtx2);
        };
    }

    public ITickableNode[] createTickableNodes() {
        ITickableNode[] nodes = new ITickableNode[variablesArray.length];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = variablesArray[i].createTickable();
        }
        return nodes;
    }

    public interface ITextureGetter {
        ModelUtil.TexturedFace get(String location);
    }
}
