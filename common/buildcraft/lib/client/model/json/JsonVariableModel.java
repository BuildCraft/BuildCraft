/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model.json;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.ResourceLoaderContext;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.json.JsonVariableObject;
import buildcraft.lib.misc.JsonUtil;

/** {@link JsonModel} but any element can change depending on variables. */
public class JsonVariableModel extends JsonVariableObject {
    // Never allow ao or textures to be variable - they need to be hardcoded so that we can stitch them
    public final boolean ambientOcclusion;
    public final Map<String, JsonTexture> textures;
    public final JsonModelRule[] rules;
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
            to[i] = JsonVariableModelPart.deserializeModelPart(array.get(i), fnCtx, ctx);
        }
        return to;
    }

    public JsonVariableModel(JsonObject obj, FunctionContext fnCtx, ResourceLoaderContext ctx)
        throws JsonParseException {
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
        finaliseVariables();

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

    public interface ITextureGetter {
        ModelUtil.TexturedFace get(String location);
    }
}
